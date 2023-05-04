package journal.gratitude.com.gratitudejournal.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.lifecycle.SavedStateHandle
import com.dropbox.core.oauth.DbxCredential
import com.presently.logging.AnalyticsLogger
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.robot.EntryRobot
import journal.gratitude.com.gratitudejournal.robot.MilestoneRobot
import journal.gratitude.com.gratitudejournal.ui.entry.Entry
import journal.gratitude.com.gratitudejournal.ui.entry.EntryViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import kotlin.test.fail

class EntryUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private val analytics = object : AnalyticsLogger {
        override fun recordEvent(event: String) {}

        override fun recordEvent(event: String, details: Map<String, Any>) {}

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

        override fun recordEntryAdded(numEntries: Int) {}

        override fun recordView(viewName: String) {}

        override fun optOutOfAnalytics() {}

        override fun optIntoAnalytics() {}
    }

    private val settings = object : PresentlySettings {
        override fun getCurrentTheme(): String = "Original"

        override fun setTheme(themeName: String) = fail("Not needed in this test")

        override fun isBiometricsEnabled(): Boolean = fail("Not needed in this test")

        override fun shouldLockApp(): Boolean = fail("Not needed in this test")

        override fun onAppBackgrounded() = fail("Not needed in this test")

        override fun onAuthenticationSucceeded() = fail("Not needed in this test")

        override fun shouldShowQuote(): Boolean = true

        override fun getAutomaticBackupCadence(): BackupCadence = fail("Not needed in this test")

        override fun getLocale(): String = fail("Not needed in this test")

        override fun hasEnabledNotifications(): Boolean = fail("Not needed in this test")

        override fun getNotificationTime(): LocalTime = fail("Not needed in this test")

        override fun hasUserDisabledAlarmReminders(context: Context): Boolean =
            fail("Not needed in this test")

        override fun getLinesPerEntryInTimeline(): Int = fail("Not needed in this test")

        override fun shouldShowDayOfWeekInTimeline(): Boolean = fail("Not needed in this test")

        override fun getAccessToken(): DbxCredential? = fail("Not needed in this test")

        override fun setAccessToken(newToken: DbxCredential) = fail("Not needed in this test")

        override fun wasDropboxAuthInitiated(): Boolean = fail("Not needed in this test")

        override fun markDropboxAuthAsCancelled() = fail("Not needed in this test")

        override fun markDropboxAuthInitiated() = fail("Not needed in this test")

        override fun clearAccessToken() = fail("Not needed in this test")

        override fun isOptedIntoAnalytics(): Boolean = fail("Not needed in this test")
    }

    @Test
    fun entryIntegrationTest() {
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return null
            }
            override suspend fun addEntry(entry: Entry): Int = 1
            override fun getEntriesFlow(): Flow<List<Entry>> = fail("not needed in this test")
            override suspend fun getEntries(): List<Entry> = fail("not needed in this test")
            override suspend fun addEntries(entries: List<Entry>) = fail("not needed in this test")
            override suspend fun search(query: String): List<Entry> = fail("not needed in this test")
        }

        val viewModel = EntryViewModel(
            SavedStateHandle(),
            repository,
            analytics,
            settings
        )

        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    viewModel = viewModel,
                    onEntryExit = {}
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)

        entryRobot.assertCorrectDateIsShown(today)
        entryRobot.assertUserIsInEditMode()

        entryRobot.type("Hello there!")

        // type some more and then undo
        entryRobot.type(" More text")
        entryRobot.assertEntryEditTextEquals("Hello there! More text")
        entryRobot.clickUndo()
        entryRobot.assertEntryEditTextEquals("Hello there!")

        entryRobot.exitEditMode()
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
    }

    @Test
    fun milestoneTest() = runTest {
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return null
            }
            override suspend fun addEntry(entry: Entry): Int = 5
            override fun getEntriesFlow(): Flow<List<Entry>> = fail("not needed in this test")
            override suspend fun getEntries(): List<Entry> = fail("not needed in this test")
            override suspend fun addEntries(entries: List<Entry>) = fail("not needed in this test")
            override suspend fun search(query: String): List<Entry> = fail("not needed in this test")
        }

        val viewModel = EntryViewModel(
            SavedStateHandle(),
            repository,
            analytics,
            settings
        )

        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    viewModel = viewModel,
                    onEntryExit = {}
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)
        val milestoneRobot = MilestoneRobot(composeTestRule)

        entryRobot.assertCorrectDateIsShown(today)
        entryRobot.assertUserIsInEditMode()
        entryRobot.type("Hello there!")

        entryRobot.clickBackButton()
        entryRobot.assertNotInEditMode()

        composeTestRule.onRoot().printToLog("blerg")

        // todo get milestone tests working
//        milestoneRobot.assertMilestoneScreenShown(5)
//        milestoneRobot.dismissMilestoneScreen()
    }

    @Test
    fun entryPromptTest() {
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return null
            }
            override suspend fun addEntry(entry: Entry): Int = 1
            override fun getEntriesFlow(): Flow<List<Entry>> = fail("not needed in this test")
            override suspend fun getEntries(): List<Entry> = fail("not needed in this test")
            override suspend fun addEntries(entries: List<Entry>) = fail("not needed in this test")
            override suspend fun search(query: String): List<Entry> = fail("not needed in this test")
        }
        val viewModel = EntryViewModel(
            SavedStateHandle(),
            repository,
            analytics,
            settings
        )

        composeTestRule.setContent {
            PresentlyTheme {
                Entry(
                    viewModel = viewModel,
                    onEntryExit = {}
                )
            }
        }

        val entryRobot = EntryRobot(composeTestRule)

        entryRobot.assertCorrectQuestionTense(today)
        entryRobot.type("Hello there!")
        entryRobot.clickPromptButton()

        entryRobot.assertHintWasSet()
    }
}
