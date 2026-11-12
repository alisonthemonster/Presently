package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureAcrossThemes
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

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
                onCalendarDateClick = { _, _, _ -> },
                onScrubberUsed = {}
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
                onCalendarDateClick = { _, _, _ -> },
                onScrubberUsed = {}
            )
        }
    }

    // ---- scrubber ----

    @Test
    fun scrubber_idleHandle_allThemes() {
        captureScrubber(scenario = "scrubber-idle", items = scrubberRows(), drag = false)
    }

    @Test
    fun scrubber_dragging_bubbleAndYearTicks_allThemes() {
        captureScrubber(scenario = "scrubber-dragging", items = scrubberRows(), drag = true)
    }

    @Test
    fun scrubber_dragging_denseYears_labelsThinned_allThemes() {
        captureScrubber(scenario = "scrubber-dragging-dense", items = denseYearRows(), drag = true)
    }

    /** Several years of sparse entries (one row every ~10 days) with milestone rows mixed in. */
    private fun scrubberRows(): List<TimelineRowState> {
        val rows = sparseRows(120).toMutableList()
        rows.add(10, milestoneRow(100))
        rows.add(60, milestoneRow(50))
        return rows
    }

    /** 25 years with one row each at the top, then 40 rows in one old year: labels must thin. */
    private fun denseYearRows(): List<TimelineRowState> =
        (0 until 25).map { entryRow(LocalDate.of(2026 - it, 6, 1)) } +
            (0 until 40).map { entryRow(LocalDate.of(2000, 12, 1).minusDays(it.toLong() * 3)) }

    /**
     * The scrubber auto-hides on a timer, so drive the clock by hand and re-trigger the scroll for
     * each theme instead of relying on `waitForIdle` (which would let the timer run out).
     */
    private fun captureScrubber(scenario: String, items: List<TimelineRowState>, drag: Boolean) {
        var currentTheme by mutableStateOf(PresentlyThemeSpec.entries.first())
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(width = 411.dp, height = 891.dp)) {
                PresentlyTheme(themeSpec = currentTheme) {
                    TimelineScreenContent(
                        state = TimelineUiState(items = items),
                        onSearchClick = {},
                        onSettingsClick = {},
                        onTimelineEntryClick = {},
                        onCalendarClick = {},
                        onCalendarClose = {},
                        onCalendarDateClick = { _, _, _ -> },
                        onScrubberUsed = {}
                    )
                }
            }
        }

        PresentlyThemeSpec.entries.forEach { themeSpec ->
            composeRule.runOnUiThread { currentTheme = themeSpec }
            composeRule.mainClock.advanceTimeBy(50)
            composeRule.onNodeWithTag(TimelineScreenTags.LIST).performScrollToIndex(0)
            composeRule.onNodeWithTag(TimelineScreenTags.LIST).performScrollToIndex(8)
            composeRule.mainClock.advanceTimeBy(300)
            if (drag) {
                composeRule.onNodeWithTag(TimelineScreenTags.SCRUBBER_HANDLE).performTouchInput {
                    down(center)
                    moveBy(Offset(0f, 20f))
                    moveBy(Offset(0f, 900f))
                }
                composeRule.mainClock.advanceTimeBy(100)
                composeRule.mainClock.advanceTimeBy(100)
                composeRule.mainClock.advanceTimeBy(300)
            }
            composeRule.onRoot().captureRoboImage("timeline/$scenario/${themeSpec.name.lowercase()}.png")
            if (drag) {
                composeRule.onNodeWithTag(TimelineScreenTags.SCRUBBER_HANDLE).performTouchInput { up() }
                composeRule.mainClock.advanceTimeBy(1000)
            }
        }
    }
}
