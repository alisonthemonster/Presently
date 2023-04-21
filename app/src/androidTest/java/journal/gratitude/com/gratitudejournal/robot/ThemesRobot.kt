package journal.gratitude.com.gratitudejournal.robot

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.rules.ActivityScenarioRule
import journal.gratitude.com.gratitudejournal.MainActivity

class ThemesRobot(
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {

    fun selectTheme(themeName: String) {
        composeTestRule.onNodeWithText(themeName).performClick()
    }
}