package journal.gratitude.com.gratitudejournal.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate

class TimelineRobot(
    val composeTestRule: ComposeTestRule
) {

    fun verifyCorrectTimelineState() {
        // verify fake entry from FakeEntryRepository
        composeTestRule.onNodeWithText("Monday, December 19, 2022").assertIsDisplayed()
        composeTestRule.onNodeWithText("A test entry on the 19th of December.").assertIsDisplayed()

        // verify we're showing today and yesterday items
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        assertTimelineHasEntry(today, "What are you grateful for?")
        assertTimelineHasEntry(yesterday, "What were you grateful for?")
    }

    fun clickTodayEntry() {
        val today = LocalDate.now()
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
