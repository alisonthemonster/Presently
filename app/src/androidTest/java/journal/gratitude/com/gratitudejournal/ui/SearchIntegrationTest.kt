package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.search.Search
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class SearchIntegrationTest {

    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun searchIntegrationTest() = runTest {
        composeTestRule.setContent {
            PresentlyTheme {
                Search(onEntryClicked = {})
            }
        }

        composeTestRule.onNodeWithTag("searchFieldTestTag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("searchFieldTestTag").performTextInput("searchQuery")

        composeTestRule.waitUntil {
            composeTestRule
                .onAllNodesWithText("A test entry on the 19th of December.")
                .fetchSemanticsNodes().size == 1
        }

        //verify search result is shown
        composeTestRule.onNodeWithText("A test entry on the 19th of December.").assertIsDisplayed()
    }
}