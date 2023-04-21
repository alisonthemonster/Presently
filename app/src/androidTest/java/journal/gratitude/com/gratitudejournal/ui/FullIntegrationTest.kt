package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.PresentlyContainer
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.navigation.UserStartDestination
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
class FullIntegrationTest {
    //so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Inject
    lateinit var repository: EntryRepository

    @Before
    fun init() = runTest {
        hiltRule.inject()
        //add some fake data
        repository.addEntries(listOf(
            Entry(LocalDate.of(2022, 10, 9), "An entry from October of 2022"),
            Entry(LocalDate.of(2022, 9, 9), "An entry from September of 2022"),
            Entry(LocalDate.of(2022, 8, 9), "An entry from August of 2022"),
            Entry(LocalDate.of(2022, 7, 9), "An entry from July of 2022"),

        ))
    }

    @Test
    fun testPresentlyFlow() {
        composeTestRule.setContent {
            PresentlyTheme {
                PresentlyContainer(UserStartDestination.DEFAULT_SCREEN)
            }
        }

        val timelineRobot = TimelineRobot(composeTestRule)
        val entryRobot = EntryRobot(composeTestRule)
        val milestoneRobot = MilestoneRobot(composeTestRule)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        timelineRobot.assertTimelineHasEntry(today, "What are you grateful for?")
        timelineRobot.assertTimelineHasEntry(yesterday, "What were you grateful for?")
        timelineRobot.assertTimelineHasEntry(LocalDate.of(2022, 10, 9), "An entry from October of 2022")
        timelineRobot.assertTimelineHasEntry(LocalDate.of(2022, 9, 9), "An entry from September of 2022")
        timelineRobot.assertTimelineHasEntry(LocalDate.of(2022, 8, 9), "An entry from August of 2022")
        timelineRobot.assertTimelineHasEntry(LocalDate.of(2022, 7, 9), "An entry from July of 2022")

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

        milestoneRobot.assertMilestoneScreenShown(5)
        milestoneRobot.dismissMilestoneScreen()

        entryRobot.assertEntryReadTextEquals("Hello there, this is the Presently integration test! More text!")

        entryRobot.exitEntryScreen()

        timelineRobot.waitForTimelineScreen()
        timelineRobot.assertTimelineHasEntry(LocalDate.now(), "Hello there, this is the Presently integration test! More text!")
    }
}