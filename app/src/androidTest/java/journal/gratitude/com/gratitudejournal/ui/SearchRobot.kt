package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import journal.gratitude.com.gratitudejournal.MainActivity

class SearchRobot(
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    fun assertSearchViewIsShown() {
        composeTestRule.onNodeWithTag("searchFieldTestTag").assertIsDisplayed()
    }

    fun performSearch(text: String) {
        composeTestRule.onNodeWithTag("searchFieldTestTag").performTextInput(text)
    }

    fun assertSearchResultIsShown(text: String) {
        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithText(text)
                .fetchSemanticsNodes().size == 1
        }
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun clickSearchResult(text: String) {
        composeTestRule.onNodeWithText(text).performClick()
    }

    fun exitSearchScreen() {
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }
}