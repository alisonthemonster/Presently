package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.timeline.Timeline
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

/**
 * Tests the integration with the repository layer.
 */
@HiltAndroidTest
class TimelineIntegrationTest {

    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun timelineIntegrationTest() = runTest {
        composeTestRule.setContent {
            PresentlyTheme {
                Timeline(
                    onEntryClicked = {},
                    onSearchClicked = {},
                    onThemesClicked = {},
                    onSettingsClicked = {},
                    onContactClicked = {}
                )
            }
        }

        //verify fake entry from FakeEntryRepository
        composeTestRule.onNodeWithText("Monday, December 19, 2022").assertIsDisplayed()
        composeTestRule.onNodeWithText("A test entry on the 19th of December.").assertIsDisplayed()

        //verify we're showing today and yesterday items
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        composeTestRule.onNodeWithText(today.toStringWithDayOfWeek()).assertIsDisplayed()
        composeTestRule.onNodeWithText(yesterday.toStringWithDayOfWeek()).assertIsDisplayed()
        composeTestRule.onNodeWithText("What are you grateful for?").assertIsDisplayed()
        composeTestRule.onNodeWithText("What were you grateful for?").assertIsDisplayed()
    }
}