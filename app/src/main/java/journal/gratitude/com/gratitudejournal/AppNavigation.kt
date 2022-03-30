package journal.gratitude.com.gratitudejournal

import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
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
    Log.d("blerg", "AppNavigation")
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
            })
        }
    }
}