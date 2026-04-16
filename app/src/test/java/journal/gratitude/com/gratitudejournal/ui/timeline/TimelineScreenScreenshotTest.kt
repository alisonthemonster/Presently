package journal.gratitude.com.gratitudejournal.ui.timeline

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

/**
 * To update tests run ./gradlew :app:recordRoborazziDebug --tests 'journal.gratitude.com.gratitudejournal.ui.timeline.TimelineScreenScreenshotTest'
 *
 * */

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class TimelineScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun entries_allThemes() {
        composeRule.captureAcrossThemes(screen = "timeline", scenario = "entries") { _ ->
            TimelineScreenContent(
                state = TimelineUiState(
                    items = listOf(
                        TimelineMilestoneRowState(number = 5, numberText = "5"),
                        TimelineEntryRowState(
                            date = LocalDate.of(2026, 3, 28),
                            dateText = "March 28, 2026",
                            content = "Sunshine on the balcony",
                            emptyHint = null,
                            isCurrentDate = false,
                            isNewEntry = false,
                            numberExistingEntries = 5,
                            maxLines = 10,
                            isLastItem = false
                        ),
                        TimelineEntryRowState(
                            date = LocalDate.of(2026, 3, 27),
                            dateText = "March 27, 2026",
                            content = "A quiet breakfast and a long walk.",
                            emptyHint = null,
                            isCurrentDate = false,
                            isNewEntry = false,
                            numberExistingEntries = 5,
                            maxLines = 10,
                            isLastItem = true
                        )
                    ),
                    writtenDates = listOf(
                        LocalDate.of(2026, 3, 28),
                        LocalDate.of(2026, 3, 27)
                    )
                ),
                onSearchClick = {},
                onSettingsClick = {},
                onTimelineEntryClick = {},
                onCalendarClick = {},
                onCalendarClose = {},
                onCalendarDateClick = { _, _, _ -> }
            )
        }
    }

    @Test
    fun emptyState_allThemes() {
        composeRule.captureAcrossThemes(screen = "timeline", scenario = "empty-state") { _ ->
            TimelineScreenContent(
                state = TimelineUiState(
                    items = listOf(
                        TimelineEntryRowState(
                            date = LocalDate.of(2026, 3, 28),
                            dateText = "March 28, 2026",
                            content = "",
                            emptyHint = journal.gratitude.com.gratitudejournal.R.string.what_are_you_thankful_for_today,
                            isCurrentDate = true,
                            isNewEntry = true,
                            numberExistingEntries = 0,
                            maxLines = 10,
                            isLastItem = false
                        ),
                        TimelineEntryRowState(
                            date = LocalDate.of(2026, 3, 27),
                            dateText = "March 27, 2026",
                            content = "",
                            emptyHint = journal.gratitude.com.gratitudejournal.R.string.what_are_you_thankful_for_yesterday,
                            isCurrentDate = false,
                            isNewEntry = true,
                            numberExistingEntries = 0,
                            maxLines = 10,
                            isLastItem = true
                        )
                    )
                ),
                onSearchClick = {},
                onSettingsClick = {},
                onTimelineEntryClick = {},
                onCalendarClick = {},
                onCalendarClose = {},
                onCalendarDateClick = { _, _, _ -> }
            )
        }
    }
}
