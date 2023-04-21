package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick

class MilestoneRobot(private val composeTestRule: ComposeTestRule) {

    fun assertMilestoneScreenShown(milestone: Int) {
        composeTestRule.onNodeWithTag("milestoneScreen").assertIsDisplayed()
        composeTestRule.onNodeWithText(milestone.toString()).assertIsDisplayed()
        composeTestRule.onNodeWithText("$milestone days of gratitude!").assertIsDisplayed()
    }

    fun dismissMilestoneScreen() {
        composeTestRule.onNodeWithContentDescription("Close").performClick()
    }
}