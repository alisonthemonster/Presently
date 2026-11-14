package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.down
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.up
import androidx.compose.ui.test.center
import androidx.compose.runtime.mutableStateOf
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class TimelineScrubberComposeTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val handle = TimelineScreenTags.SCRUBBER_HANDLE
    private val bubble = TimelineScreenTags.SCRUBBER_BUBBLE
    private val a11y = TimelineScreenTags.SCRUBBER_SEMANTICS

    private var scrubberUsedCount = 0

    private fun setScreen(items: List<TimelineRowState>, calendarVisible: Boolean = false) {
        // The auto-hide timer uses the test clock: drive it by hand.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PresentlyTheme(themeSpec = PresentlyThemeSpec.entries.first()) {
                TimelineScreenContent(
                    state = TimelineUiState(items = items, isCalendarVisible = calendarVisible),
                    onSearchClick = {},
                    onSettingsClick = {},
                    onTimelineEntryClick = {},
                    onCalendarClick = {},
                    onCalendarClose = {},
                    onCalendarDateClick = { _, _, _ -> },
                    onScrubberUsed = { scrubberUsedCount++ }
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(100)
    }

    private fun advance(ms: Long) = composeRule.mainClock.advanceTimeBy(ms)

    /**
     * Steps the manual clock in small increments (one big jump would skip animation frames) until
     * [condition] holds, polling the semantics tree like a real consumer would.
     */
    private fun advanceUntil(maxMs: Long, stepMs: Long = 50, condition: () -> Boolean): Boolean {
        var elapsed = 0L
        while (elapsed < maxMs) {
            if (condition()) return true
            advance(stepMs)
            elapsed += stepMs
        }
        return condition()
    }

    private fun scrollList(index: Int) {
        composeRule.onNodeWithTag(TimelineScreenTags.LIST).performScrollToIndex(index)
        advance(300)
    }

    @Test
    fun shortList_neverShowsScrubber() {
        setScreen(dailyRows(MIN_ROWS_FOR_SCRUBBER - 1))
        scrollList(5)
        composeRule.onNodeWithTag(handle).assertDoesNotExist()
        composeRule.onNodeWithTag(a11y).assertDoesNotExist()
    }

    @Test
    fun longList_hiddenAtRest_visibleAfterScroll_hiddenAgainAfterIdle() {
        setScreen(dailyRows(MIN_ROWS_FOR_SCRUBBER + 70))
        composeRule.onNodeWithTag(handle).assertDoesNotExist()

        scrollList(20)
        composeRule.onNodeWithTag(handle).assertIsDisplayed()

        advance(SCRUBBER_HIDE_DELAY_MS + 500)
        composeRule.onNodeWithTag(handle).assertDoesNotExist()
    }

    @Test
    fun calendarVisible_hidesScrubber() {
        setScreen(dailyRows(100), calendarVisible = true)
        scrollList(20)
        advance(500)
        composeRule.onNodeWithTag(handle).assertDoesNotExist()
        composeRule.onNodeWithTag(bubble).assertDoesNotExist()
    }

    @Test
    fun dragging_scrollsListAndShowsBubble_thenBubbleDisappears() {
        val rows = sparseRows(100)
        setScreen(rows)
        scrollList(1)
        composeRule.onNodeWithTag(TimelineScreenTags.entry("2026-03-28")).assertDoesNotExist()

        composeRule.onNodeWithTag(handle).performTouchInput {
            down(center)
            moveBy(Offset(0f, 20f))
            moveBy(Offset(0f, 5000f)) // far past the end: clamps to the oldest row
        }
        advance(300)

        val oldest = (rows.last() as TimelineEntryRowState).date
        val expected = java.time.YearMonth.of(oldest.year, oldest.month)
        val text = composeRule.onNodeWithTag(bubble).fetchSemanticsNode()
            .config[SemanticsProperties.Text].joinToString("")
        assertEquals(
            TimelineScrubberLabels(java.util.Locale.getDefault()).monthYear(expected),
            text
        )
        // the list followed the drag to the oldest row
        composeRule.onNodeWithTag(TimelineScreenTags.entry(oldest.toString())).assertIsDisplayed()
        assertEquals(1, scrubberUsedCount)

        composeRule.onNodeWithTag(handle).performTouchInput { up() }
        advance(SCRUBBER_OVERLAY_LINGER_MS + 500)
        composeRule.onNodeWithTag(bubble).assertDoesNotExist()
    }

    @Test
    fun dragBackToTop_showsNewestMonth() {
        val rows = sparseRows(100)
        setScreen(rows)
        scrollList(60)
        composeRule.onNodeWithTag(handle).performTouchInput {
            down(center)
            moveBy(Offset(0f, -20f))
            moveBy(Offset(0f, -5000f))
        }
        advance(300)
        composeRule.onNodeWithTag(TimelineScreenTags.entry("2026-03-28")).assertIsDisplayed()
        composeRule.onNodeWithTag(handle).performTouchInput { up() }
    }

    @Test
    fun setProgress_scrollsList() {
        val rows = sparseRows(100)
        setScreen(rows)
        // present for TalkBack even while the visual handle is hidden
        composeRule.onNodeWithTag(a11y).assertExists()
        composeRule.onNodeWithTag(handle).assertDoesNotExist()

        composeRule.onNodeWithTag(a11y).performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        advance(300)
        val oldest = (rows.last() as TimelineEntryRowState).date
        composeRule.onNodeWithTag(TimelineScreenTags.entry(oldest.toString())).assertIsDisplayed()

        val config = composeRule.onNodeWithTag(a11y).fetchSemanticsNode().config
        assertTrue(config.contains(SemanticsProperties.StateDescription))
        assertTrue(config.contains(SemanticsProperties.ContentDescription))
    }

    @Test
    fun listOfPlaceholderRowsOnly_doesNotCrash() {
        setScreen(listOf(entryRow(LocalDate.of(2026, 3, 28), ""), entryRow(LocalDate.of(2026, 3, 27), "")))
        scrollList(0)
        composeRule.onNodeWithTag(handle).assertDoesNotExist()
    }

    @Test
    fun itemsChangingMidDrag_doesNotCrash() {
        val items = mutableStateOf(sparseRows(100))
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PresentlyTheme(themeSpec = PresentlyThemeSpec.entries.first()) {
                TimelineScreenContent(
                    state = TimelineUiState(items = items.value),
                    onSearchClick = {}, onSettingsClick = {}, onTimelineEntryClick = {},
                    onCalendarClick = {}, onCalendarClose = {}, onCalendarDateClick = { _, _, _ -> },
                    onScrubberUsed = {}
                )
            }
        }
        advance(100)
        scrollList(10)
        composeRule.onNodeWithTag(handle).performTouchInput {
            down(center)
            moveBy(Offset(0f, 20f))
            moveBy(Offset(0f, 200f))
        }
        advance(100)
        composeRule.runOnIdle { items.value = sparseRows(40) + sparseRows(3, LocalDate.of(2020, 1, 1)) }
        advance(100)
        composeRule.onNodeWithTag(handle).performTouchInput { moveBy(Offset(0f, 5000f)) }
        advance(100)
        composeRule.onNodeWithTag(bubble).assertExists()
        composeRule.onNodeWithTag(handle).performTouchInput { up() }
        composeRule.runOnIdle { items.value = emptyList() }
        advance(1000)
    }

    private fun calendarButtonHidden(): Boolean = composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON)
        .fetchSemanticsNode().config.contains(SemanticsProperties.InvisibleToUser)

    @Test
    fun calendarButton_hiddenWhileScrubbing_andBackAfterwards() {
        setScreen(sparseRows(100))
        assertTrue(!calendarButtonHidden())

        scrollList(1)
        composeRule.onNodeWithTag(handle).performTouchInput {
            down(center)
            moveBy(Offset(0f, 20f))
            moveBy(Offset(0f, 200f))
        }
        advance(400)
        assertTrue(calendarButtonHidden())

        // Held still: still hidden, even though the list is no longer scrolling.
        advance(2000)
        assertTrue(calendarButtonHidden())

        composeRule.onNodeWithTag(handle).performTouchInput { up() }
        assertTrue(advanceUntil(3000) { !calendarButtonHidden() })
    }

    @Test
    fun calendarButton_comesBack_ifScrubberLeavesCompositionMidDrag() {
        val items = mutableStateOf(sparseRows(100))
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PresentlyTheme(themeSpec = PresentlyThemeSpec.entries.first()) {
                TimelineScreenContent(
                    state = TimelineUiState(items = items.value),
                    onSearchClick = {}, onSettingsClick = {}, onTimelineEntryClick = {},
                    onCalendarClick = {}, onCalendarClose = {}, onCalendarDateClick = { _, _, _ -> },
                    onScrubberUsed = {}
                )
            }
        }
        advance(100)
        scrollList(10)
        composeRule.onNodeWithTag(handle).performTouchInput {
            down(center)
            moveBy(Offset(0f, 20f))
            moveBy(Offset(0f, 200f))
        }
        advance(400)
        assertTrue(calendarButtonHidden())

        // Below the threshold the scrubber is removed while the finger is still down.
        composeRule.runOnIdle { items.value = sparseRows(10) }
        assertTrue(advanceUntil(3000) { !calendarButtonHidden() })
    }
}
