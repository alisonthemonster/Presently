package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
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
//todo where do the dropbox warning notifs go to?

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
    startDestination: String,
    cameFromNotification: Boolean,
) {
    val activity = LocalContext.current as Activity
    val resources = activity.resources
    val configuration = resources.configuration
    val locale = configuration.locales[0]

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
                },
                onContactClicked = {
                    onContactClicked(activity)
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

private fun onContactClicked(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")

        val emails = arrayOf("gratitude.journal.app@gmail.com")
        val subject = "In App Feedback"
        putExtra(Intent.EXTRA_EMAIL, emails)
        putExtra(Intent.EXTRA_SUBJECT, subject)

        val packageName = context.packageName
        val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
        val text = """
                Device: ${Build.MODEL}
                OS Version: ${Build.VERSION.RELEASE}
                App Version: ${packageInfo.versionName}
                
                
                """.trimIndent()
        putExtra(Intent.EXTRA_TEXT, text)
    }

    try {
        context.startActivity(intent)
    } catch (activityNotFoundException: ActivityNotFoundException) {
        //crashReporter.logHandledException(activityNotFoundException)
        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun SettingsFragmentContainer() {
    //todo settings is not styled anymore
    AndroidViewBinding(FragmentSettingsBinding::inflate)
}