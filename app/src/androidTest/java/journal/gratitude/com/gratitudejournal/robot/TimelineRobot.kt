package journal.gratitude.com.gratitudejournal.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class TimelineRobot(
    val composeTestRule: ComposeTestRule
) {

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val yesterday = today.minus(1, DateTimeUnit.DAY)
    fun verifyCorrectTimelineState() {
        // verify fake entry from FakeEntryRepository
        composeTestRule.onNodeWithText("Monday, December 19, 2022").assertIsDisplayed()
        composeTestRule.onNodeWithText("A test entry on the 19th of December.").assertIsDisplayed()

        // verify we're showing today and yesterday items
        assertTimelineHasEntry(today, "What are you grateful for?")
        assertTimelineHasEntry(yesterday, "What were you grateful for?")
    }

    fun clickTodayEntry() {
        composeTestRule.onNodeWithText(today.toFullString()).performClick()
    }

    fun assertTimelineHasEntry(date: LocalDate, text: String) {
        composeTestRule.onNodeWithText(date.toFullString()).assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun waitForTimelineScreen() {
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithTag("timelineList")
                .fetchSemanticsNodes().size == 1
        }
    }

    fun launchSearch() {
        clickHamburger()
        composeTestRule.onNodeWithText("Search").performClick()
    }

    fun clickHamburger() {
        composeTestRule.onNodeWithContentDescription("Menu").performClick()
    }

    fun launchThemesScreen() {
        clickHamburger()
        composeTestRule.onNodeWithText("Theme").performClick()
    }

    fun launchContactEmail() {
        clickHamburger()
        composeTestRule.onNodeWithText("Contact Us").performClick()
    }

    fun launchSettings() {
        clickHamburger()
        composeTestRule.onNodeWithText("Settings").performClick()
    }
}
