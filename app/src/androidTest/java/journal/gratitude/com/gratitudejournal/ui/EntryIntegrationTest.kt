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
import org.threeten.bp.LocalDate
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

        val entryRobot = EntryRobot(composeTestRule)

        entryRobot.assertCorrectDateIsShown(LocalDate.now())
        entryRobot.assertCorrectTenseIsUsed(LocalDate.now())
        entryRobot.assertUserIsInViewMode()

        entryRobot.enterEditMode()
        entryRobot.assertUserIsInEditMode()

        entryRobot.type("Hello there!")

        //type some more and then undo
        entryRobot.type("editViewTextField")
        entryRobot.assertEntryEditTextEquals("Hello there! More text")
        entryRobot.clickUndo()
        entryRobot.assertEntryEditTextEquals("Hello there!")

        //tap redo
//        composeTestRule.onNodeWithContentDescription("Redo").performClick()
//        composeTestRule.onNodeWithTag("editViewTextField").assertTextEquals("Hello there! More text")

        entryRobot.exitEditMode()
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()

    }
}