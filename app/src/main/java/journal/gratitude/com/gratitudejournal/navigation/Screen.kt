package journal.gratitude.com.gratitudejournal.navigation

import journal.gratitude.com.gratitudejournal.util.toDatabaseString
import org.threeten.bp.LocalDate

internal sealed class Screen(val route: String) {
    fun createRoute() = route

    object Timeline : Screen("timeline")

    object Entry : Screen("entry/{entry-date}") {
        fun createRoute(entryDate: LocalDate): String {
            return "entry/${entryDate.toDatabaseString()}"
        }
    }

    object MilestoneCelebration : Screen("milestone/{number}") {
        fun createRoute(milestoneNumber: Int): String {
            return "milestone/$milestoneNumber"
        }
    }

    object Settings : Screen("settings")
    object Search : Screen("search")
    object Themes : Screen("themes")
    object Lock : Screen("lock")
}

enum class UserStartDestination {
    ENTRY_SCREEN,
    SETTINGS_SCREEN,
    DEFAULT_SCREEN
}
