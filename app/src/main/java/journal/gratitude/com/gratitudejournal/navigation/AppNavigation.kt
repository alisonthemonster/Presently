package journal.gratitude.com.gratitudejournal.navigation

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.FragmentSettingsBinding
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import journal.gratitude.com.gratitudejournal.ui.search.Search
import journal.gratitude.com.gratitudejournal.ui.security.AppLockScreen
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeSelection
import journal.gratitude.com.gratitudejournal.ui.timeline.Timeline
import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import org.threeten.bp.LocalDate

// todo test with other bottom gesture navs

@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String,
    postAuthDestination: UserStartDestination,
) {
    val activity = LocalContext.current as Activity

    AnimatedNavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
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
                },
                onContactClicked = {
                    onContactClicked(activity)
                },
            )
        }
        composable(
            route = Screen.Entry.createRoute(),
            arguments = listOf(
                navArgument("entry-date") { type = NavType.StringType },
            ),
        ) {
            Entry(
                onEntryExit = {
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Screen.Search.route,
        ) {
            Search(
                onEntryClicked = { date ->
                    navController.navigate(Screen.Entry.createRoute(date))
                },
                onBackClicked = {
                    navController.navigateUp()
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
                },
            )
        }
        composable(
            route = Screen.Settings.route,
        ) {
            SettingsFragmentContainer()
        }
        composable(
            route = Screen.Lock.route,
        ) {
            AppLockScreen(
                onUserAuthenticated = {
                    when (postAuthDestination) {
                        UserStartDestination.ENTRY_SCREEN -> {
                            navController.navigate(Screen.Entry.createRoute(LocalDate.now())) {
                                popUpTo(0) // reset stack
                            }
                        }
                        UserStartDestination.SETTINGS_SCREEN -> {
                            navController.navigate(Screen.Settings.createRoute()) {
                                popUpTo(0) // reset stack
                            }
                        }
                        UserStartDestination.DEFAULT_SCREEN -> {
                            navController.navigate(Screen.Timeline.createRoute()) {
                                popUpTo(0) // reset stack
                            }
                        }
                    }
                },
                onUserAuthenticationFailed = {
                    it?.let {
                        Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
                    }
                    activity.finish()
                },
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
        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
    }
}

internal class EntryArgs(val entryDate: String) {
    constructor(savedStateHandle: SavedStateHandle) : this(savedStateHandle.getDateString())
}

private fun SavedStateHandle.getDateString(): String {
    return if (this.contains("entry-date")) {
        checkNotNull(this["entry-date"]) as String
    } else {
        LocalDate.now().toDatabaseString()
    }
}

internal class MilestoneArgs(val milestoneNumber: Int) {
    constructor(savedStateHandle: SavedStateHandle) :
        this(checkNotNull(savedStateHandle["number"]) as Int)
}

@Composable
fun SettingsFragmentContainer() {
    AndroidViewBinding(
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeContent),
        factory = FragmentSettingsBinding::inflate,
    )
}
