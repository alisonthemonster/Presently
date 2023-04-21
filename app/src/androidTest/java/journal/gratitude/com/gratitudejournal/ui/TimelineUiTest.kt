package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.presently.ui.OriginalColors
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.timeline.MilestoneRow
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class TimelineUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testMilestoneRow() {
        composeTestRule.setContent {
            PresentlyTheme {
                MilestoneRow(
                    theme = OriginalColors,
                    milestoneNumber = 150
                )
            }
        }
        composeTestRule.onNodeWithText("150 days of gratitude").assertIsDisplayed()
    }


}