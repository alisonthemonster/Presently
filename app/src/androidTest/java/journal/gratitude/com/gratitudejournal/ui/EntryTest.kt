package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class EntryIntegrationTest {
    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun entryIntegrationTest() = runTest {
        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    onEntryExit = {},
                    onShareClicked = {_, _ -> }
                )
            }
        }

        //verify the view mode looks correct
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithText("I am grateful for").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertDoesNotExist()

        //enter edit mode
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        //verify edit mode looks correct
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Undo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Redo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Get a prompt").assertIsDisplayed()

        //start typing
        composeTestRule.onNodeWithTag("editViewTextField").performTextInput("Hello there!")

        //type some more and then undo
        composeTestRule.onNodeWithTag("editViewTextField").performTextInput(" More text")
        composeTestRule.onNodeWithTag("editViewTextField").assertTextEquals("Hello there! More text")
        composeTestRule.onNodeWithContentDescription("Undo").performClick()
        composeTestRule.onNodeWithTag("editViewTextField").assertTextEquals("Hello there!")

        //tap redo
        composeTestRule.onNodeWithContentDescription("Redo").performClick()
        composeTestRule.onNodeWithTag("editViewTextField").assertTextEquals("Hello there! More text")

        //navigate back to view screen
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.onNodeWithText("Hello there! More text").assertIsDisplayed()

    }
}