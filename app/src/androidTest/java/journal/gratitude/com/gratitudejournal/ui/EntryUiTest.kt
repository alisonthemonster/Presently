package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.robot.EntryRobot
import journal.gratitude.com.gratitudejournal.robot.MilestoneRobot
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
class EntryUiTest {
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
    fun entryIntegrationTest() {
        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    onEntryExit = {},
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)

        entryRobot.assertCorrectDateIsShown(LocalDate.now())
        entryRobot.assertUserIsInEditMode()

        entryRobot.type("Hello there!")

        //type some more and then undo
        entryRobot.type(" More text")
        entryRobot.assertEntryEditTextEquals("Hello there! More text")
        entryRobot.clickUndo()
        entryRobot.assertEntryEditTextEquals("Hello there!")

        entryRobot.exitEditMode()
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()

    }

    @Test
    fun milestoneTest() = runTest {
        //set up with four entries in db already
        repository.addEntries(
            listOf(
                journal.gratitude.com.gratitudejournal.model.Entry(LocalDate.of(2022, 10, 9), "An entry from October of 2022"),
                journal.gratitude.com.gratitudejournal.model.Entry(LocalDate.of(2022, 9, 9), "An entry from September of 2022"),
                journal.gratitude.com.gratitudejournal.model.Entry(LocalDate.of(2022, 8, 9), "An entry from August of 2022"),
                journal.gratitude.com.gratitudejournal.model.Entry(LocalDate.of(2022, 7, 9), "An entry from July of 2022"),
            )
        )

        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    onEntryExit = {},
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)
        val milestoneRobot = MilestoneRobot(composeTestRule)

        entryRobot.assertCorrectDateIsShown(LocalDate.now())
        entryRobot.assertUserIsInEditMode()
        entryRobot.type("Hello there!")

        entryRobot.clickBackButton()
        entryRobot.assertNotInEditMode()

        composeTestRule.onRoot().printToLog("blerg")

        //todo get milestone tests working
//        milestoneRobot.assertMilestoneScreenShown(5)
//        milestoneRobot.dismissMilestoneScreen()
    }

    @Test
    fun entryPromptTest() {
        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    onEntryExit = {},
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)

        entryRobot.assertCorrectQuestionTense(LocalDate.now())
        entryRobot.type("Hello there!")
        entryRobot.clickPromptButton()

        entryRobot.assertHintWasSet()
    }
}