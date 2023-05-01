package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import com.dropbox.dropshots.Dropshots
import com.google.common.truth.Truth.assertThat
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.PresentlyContainer
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.navigation.UserStartDestination
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.robot.EntryRobot
import journal.gratitude.com.gratitudejournal.robot.SearchRobot
import journal.gratitude.com.gratitudejournal.robot.ThemesRobot
import journal.gratitude.com.gratitudejournal.robot.TimelineRobot
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltAndroidTest
class FullIntegrationTest {
    // so that it uses the fake dependencies
    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val dropshots = Dropshots()

    @Inject
    lateinit var repository: EntryRepository

    @Inject
    lateinit var settings: PresentlySettings

    @Before
    fun init() = runTest {
        hiltRule.inject()
        // add some fake data
        repository.addEntries(
            listOf(
                Entry(LocalDate.of(2022, 10, 9), "An entry from October of 2022"),
            ),
        )
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
        val searchRobot = SearchRobot(composeTestRule)
        val themeRobot = ThemesRobot(composeTestRule)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        timelineRobot.assertTimelineHasEntry(today, "What are you grateful for?")
        timelineRobot.assertTimelineHasEntry(yesterday, "What were you grateful for?")
        timelineRobot.assertTimelineHasEntry(LocalDate.of(2022, 10, 9), "An entry from October of 2022")

        timelineRobot.clickTodayEntry()

        entryRobot.assertCorrectDateIsShown(LocalDate.now())
        entryRobot.assertUserIsInEditMode() // user enters edit mode when its empty

        entryRobot.type("Hello there, this is the Presently integration test!")
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test!")
        entryRobot.assertUndoIsEnabled()

        entryRobot.type(" More text!")

        entryRobot.clickUndo()
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test!")

        entryRobot.clickRedo()
        entryRobot.assertEntryEditTextEquals("Hello there, this is the Presently integration test! More text!")

        entryRobot.clickBackButton()

        // assert view mode
        entryRobot.assertEntryReadTextEquals("Hello there, this is the Presently integration test! More text!")

        entryRobot.clickBackButton()

        timelineRobot.waitForTimelineScreen()
        composeTestRule.onRoot().printToLog("blerg")
        timelineRobot.assertTimelineHasEntry(LocalDate.now(), "Hello there, this is the Presently integration test! More text!")

        timelineRobot.launchSearch()
        searchRobot.assertSearchViewIsShown()
        searchRobot.performSearch("October")

        searchRobot.assertSearchResultIsShown("An entry from October of 2022")
        searchRobot.clickSearchResult("An entry from October of 2022")
        entryRobot.assertEntryReadTextEquals("An entry from October of 2022")

        entryRobot.exitEntryScreen()
        searchRobot.assertSearchViewIsShown()

        searchRobot.exitSearchScreen()
        composeTestRule.onRoot().printToLog("blerg")
        timelineRobot.waitForTimelineScreen()

        timelineRobot.launchThemesScreen()
        themeRobot.selectTheme("Boo")

        assertThat(settings.getCurrentTheme()).isEqualTo("Boo")

        // todo figure out a way to test opening contact us and opening settings
    }
}
