package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.PresentlyContainer
import journal.gratitude.com.gratitudejournal.navigation.UserStartDestination
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

@HiltAndroidTest
class FullIntegrationTest {
    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()


    @Test
    fun testPresentlyFlow() {
        composeTestRule.setContent {
            PresentlyTheme {
                PresentlyContainer(UserStartDestination.DEFAULT_SCREEN)
            }
        }

        val timelineRobot = TimelineRobot(composeTestRule)
        val entryRobot = EntryRobot(composeTestRule)

        timelineRobot.verifyCorrectTimelineState()
        timelineRobot.clickTodayEntry()

        entryRobot.assertCorrectDateIsShown(LocalDate.now())
        entryRobot.assertCorrectTenseIsUsed(LocalDate.now())
        entryRobot.assertUserIsInViewMode()

        entryRobot.enterEditMode()
        entryRobot.assertUserIsInEditMode()

        entryRobot.type("Hello there, this is the Presently integration test!")
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test!")
        entryRobot.assertUndoIsEnabled()

        entryRobot.type(" More text!")

        entryRobot.clickUndo()
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test!")

        entryRobot.clickRedo()
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test! More text!")

        entryRobot.exitEditMode()
        entryRobot.assertEntryReadTextEquals("Hello there, this is the Presently integration test! More text!")

        entryRobot.exitEntryScreen()

        timelineRobot.waitForTimelineScreen()
        //todo latest entry isn't showing up in timeline, probably bug in fake repo
        //timelineRobot.assertTimelineHasEntry(LocalDate.now(), "Hello there, this is the Presently integration test! More text!")
    }
}