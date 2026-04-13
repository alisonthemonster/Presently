package journal.gratitude.com.gratitudejournal.ui.calendar

import androidx.compose.ui.test.junit4.createComposeRule
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureAcrossThemes
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.threeten.bp.LocalDate
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class EntryCalendarScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun calendar_withEntries_allThemes() {
        composeRule.captureAcrossThemes(screen = "calendar", scenario = "with-entries") { _ ->
            EntryCalendar(
                writtenDates = listOf(
                    LocalDate.of(2026, 4, 1),
                    LocalDate.of(2026, 4, 5),
                    LocalDate.of(2026, 4, 10),
                    LocalDate.of(2026, 4, 11),
                    LocalDate.of(2026, 3, 28),
                ),
                firstDayOfWeek = DayOfWeek.MONDAY,
                onDateClick = { _, _, _ -> },
                onCloseClick = {}
            )
        }
    }

    @Test
    fun calendar_empty_allThemes() {
        composeRule.captureAcrossThemes(screen = "calendar", scenario = "empty") { _ ->
            EntryCalendar(
                writtenDates = emptyList(),
                firstDayOfWeek = DayOfWeek.MONDAY,
                onDateClick = { _, _, _ -> },
                onCloseClick = {}
            )
        }
    }

    @Test
    fun calendar_sundayFirst_allThemes() {
        composeRule.captureAcrossThemes(screen = "calendar", scenario = "sunday-first") { _ ->
            EntryCalendar(
                writtenDates = listOf(
                    LocalDate.of(2026, 4, 5),
                    LocalDate.of(2026, 4, 10),
                ),
                firstDayOfWeek = DayOfWeek.SUNDAY,
                onDateClick = { _, _, _ -> },
                onCloseClick = {}
            )
        }
    }
}
