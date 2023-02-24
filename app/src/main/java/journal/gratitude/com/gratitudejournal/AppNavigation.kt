package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import journal.gratitude.com.gratitudejournal.databinding.FragmentSettingsBinding
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import journal.gratitude.com.gratitudejournal.ui.search.Search
import journal.gratitude.com.gratitudejournal.ui.security.AppLockScreen
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeSelection
import journal.gratitude.com.gratitudejournal.ui.timeline.Timeline
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDate

//todo fix window insets
//todo analytics
//analyticsLogger.recordEvent(LOOKED_AT_SETTINGS)

internal sealed class Screen(val route: String) {
    fun createRoute() = route

    object Timeline : Screen("timeline?milestone={milestone}")
    object Settings : Screen("settings")
    object Share : Screen("share")
    object Search : Screen("search")
    object Themes : Screen("themes")
    object Lock : Screen("lock")
    object Entry : Screen("entry/{entry-date}") {
        fun createRoute(entryDate: LocalDate): String {
            return "entry/${entryDate.toDatabaseString()}"
        }
    }
}

@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    cameFromNotification: Boolean,
) {
    val activity = LocalContext.current as Activity
    val resources = activity.resources
    val configuration = resources.configuration
    val locale = configuration.locales[0]

    //todo maybe lock here if they have biometrics enabled?
    val startDestination = if (cameFromNotification) Screen.Entry.createRoute() else Screen.Timeline.route

    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.Timeline.route,
        ) {
            Timeline(
                locale = locale,
                navController = navController,
                onEntryClicked = { date ->
                    navController.navigate(Screen.Entry.createRoute(date))
                },
                onSearchClicked = {
                    navController.navigate(Screen.Search.createRoute())
                },
                onThemesClicked = {
                    navController.navigate(Screen.Themes.createRoute())
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.createRoute())
                }
            )
        }
        composable(
            route = Screen.Entry.createRoute(),
            arguments = listOf(
                navArgument("entry-date") { type = NavType.StringType }
            )
        ) {
            val date = it.arguments?.getString("entry-date")
            Entry(
                date?.toLocalDate() ?: LocalDate.now(),
                onEntrySaved = { isNewEntry ->
                    if (isNewEntry != null) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("isNewEntry", isNewEntry)
                    }
                    navController.popBackStack()
                },
                onShareClicked = { date, content ->
                    navController.navigate(Screen.Share.createRoute())
                },
            )
        }
        composable(
            route = Screen.Share.route,
        ) {
            Column() {
                Text("SHARE")
                //todo launch the share fragment
            }
        }
        composable(
            route = Screen.Search.route,
        ) {
            Search(
                onEntryClicked = { date ->
                    navController.navigate(Screen.Entry.createRoute(date))
                },
            )
        }
        composable(
            route = Screen.Themes.route,
        ) {
            //todo add analytics
            //analytics.recordEvent(OPENED_THEMES)
            ThemeSelection(
                onThemeChanged = {
                    navController.popBackStack()
                    activity.recreate()
                }
            )
        }
        composable(
            route = Screen.Settings.route,
        ) {
            SettingsFragmentContainer()
        }
        composable(
            route = Screen.Lock.route
        ) {
            AppLockScreen(
                onUserAuthenticated = {
                    Log.d("blerg", "onUserAuthenticated: cameFromNotification - $cameFromNotification")
                    if (cameFromNotification) {
                        //take user to the entry screen
                        navController.navigate(Screen.Entry.createRoute(LocalDate.now())) {
                            popUpTo(0) //reset stack
                        }
                    } else {
                        //take them to the timeline
                        navController.navigate(Screen.Timeline.createRoute()) {
                            popUpTo(0) //reset stack
                        }
                    }
                },
                onUserAuthenticationFailed = {
                    //todo show toast with error message
                    activity.finish()
                }
            )
        }
    }
}

@Composable
fun SettingsFragmentContainer() {
    //todo settings is not styled anymore
    AndroidViewBinding(FragmentSettingsBinding::inflate)
}