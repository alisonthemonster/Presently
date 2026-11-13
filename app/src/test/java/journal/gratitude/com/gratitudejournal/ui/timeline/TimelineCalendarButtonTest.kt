package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.center
import androidx.compose.ui.test.down
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.moveBy
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.up
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
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
class TimelineCalendarButtonTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val rows: List<TimelineRowState> = List(60) { i ->
        val date = LocalDate.of(2026, 3, 28).minusDays(i.toLong())
        TimelineEntryRowState(
            date = date,
            dateText = date.toString(),
            content = "Grateful",
            emptyHint = null,
            isCurrentDate = false,
            isNewEntry = false,
            numberExistingEntries = 1,
            maxLines = 3,
            isLastItem = i == 59
        )
    }

    private fun setScreen(calendarVisible: Boolean = false) {
        // Animations use the test clock: drive it by hand.
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            PresentlyTheme(themeSpec = PresentlyThemeSpec.entries.first()) {
                TimelineScreenContent(
                    state = TimelineUiState(items = rows, isCalendarVisible = calendarVisible),
                    onSearchClick = {},
                    onSettingsClick = {},
                    onTimelineEntryClick = {},
                    onCalendarClick = {},
                    onCalendarClose = {},
                    onCalendarDateClick = { _, _, _ -> }
                )
            }
        }
        composeRule.mainClock.advanceTimeBy(100)
    }

    private fun buttonHidden(): Boolean = composeRule.onNodeWithTag(TimelineScreenTags.CALENDAR_BUTTON)
        .fetchSemanticsNode().config.contains(SemanticsProperties.InvisibleToUser)

    /** Steps the manual clock in small increments (a big jump would skip animation frames). */
    private fun advanceUntil(maxMs: Long, stepMs: Long = 50, condition: () -> Boolean): Boolean {
        var elapsed = 0L
        while (elapsed < maxMs) {
            if (condition()) return true
            composeRule.mainClock.advanceTimeBy(stepMs)
            elapsed += stepMs
        }
        return condition()
    }

    private fun dragList() {
        composeRule.onNodeWithTag(TimelineScreenTags.LIST).performTouchInput {
            down(center)
            moveBy(Offset(0f, -20f))
            moveBy(Offset(0f, -300f))
        }
    }

    @Test
    fun calendarButton_visibleAtRest() {
        setScreen()
        assertTrue(!buttonHidden())
    }

    @Test
    fun calendarButton_hiddenWhileListScrolls_andBackAfterwards() {
        setScreen()
        dragList()
        assertTrue(advanceUntil(1000) { buttonHidden() })

        composeRule.onNodeWithTag(TimelineScreenTags.LIST).performTouchInput { up() }
        assertTrue(advanceUntil(3000) { !buttonHidden() })
    }

    @Test
    fun calendarButton_notHiddenWhileCalendarIsOpen() {
        setScreen(calendarVisible = true)
        dragList()
        composeRule.mainClock.advanceTimeBy(500)
        assertTrue(!buttonHidden())
    }
}
