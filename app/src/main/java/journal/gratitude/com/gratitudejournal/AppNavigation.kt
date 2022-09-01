package journal.gratitude.com.gratitudejournal

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import journal.gratitude.com.gratitudejournal.ui.search.Search
import journal.gratitude.com.gratitudejournal.ui.settings.ThemeSelection
import journal.gratitude.com.gratitudejournal.ui.timeline.Timeline
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDate

internal sealed class Screen(val route: String) {
    fun createRoute() = route

    object Timeline : Screen("timeline")
    object Settings : Screen("settings")
    object Share : Screen("share")
    object Search : Screen("search")
    object Themes : Screen("themes")
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
) {
    val activity = LocalContext.current as Activity

    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = Screen.Timeline.route
    ) {
        composable(
            route = Screen.Timeline.route,
        ) {
            Timeline(
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
                onEntrySaved = { milestoneNumber ->
                    if (milestoneNumber != null) {
                        //todo how will we pass this back?
                    }
                    navController.navigateUp()
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
            Column() {
                Text("SETTINGS")
            }
        }
    }
}