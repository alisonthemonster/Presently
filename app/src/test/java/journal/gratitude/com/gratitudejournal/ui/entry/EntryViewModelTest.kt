package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.presently.logging.AnalyticsLogger
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.MainDispatcherRule
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import kotlin.test.fail

class EntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: EntryViewModel

    private val repository = object : EntryRepository {
        override suspend fun getEntry(date: LocalDate): Entry {
            return Entry(date, "hii there")
        }

        override fun getEntriesFlow(): Flow<List<Entry>> {
            return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
        }

        override suspend fun getEntries(): List<Entry> {
            return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
        }

        override suspend fun addEntry(entry: Entry): Int = -1

        override suspend fun addEntries(entries: List<Entry>) = Unit

        override suspend fun search(query: String): List<Entry> = emptyList()
    }

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
        override fun getCurrentTheme(): String = fail("Not needed in this test")

        override fun setTheme(themeName: String) = fail("Not needed in this test")

        override fun isBiometricsEnabled(): Boolean = fail("Not needed in this test")

        override fun shouldLockApp(): Boolean = fail("Not needed in this test")

        override fun onAppBackgrounded() = fail("Not needed in this test")

        override fun onAuthenticationSucceeded() = fail("Not needed in this test")

        override fun setOnPauseTime() = fail("Not needed in this test")

        override fun getFirstDayOfWeek(): Int = fail("Not needed in this test")

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
    fun `GIVEN entry view model WHEN changeHint is called THEN the state is updated`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)
        viewModel.changeHint(4)

        val newHintNumber = viewModel.state.value.promptNumber

        assertThat(newHintNumber).isIn(0 until 4)
    }

    @Test
    fun `GIVEN entry view model WHEN changeHint is called THEN an analytics event is logged`() {
        var recordEventWasCalled = false
        var eventName = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordEventWasCalled = true
                eventName = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {}

            override fun optOutOfAnalytics() {}

            override fun optIntoAnalytics() {}
        }

        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)
        viewModel.changeHint(4)

        assertThat(eventName).isEqualTo("clickedNewPrompt")
        assertThat(recordEventWasCalled).isTrue()
    }

    @Test
    fun `GIVEN an entry view model WHEN the view model is created THEN the entry is fetched`() {
        var getEntryWasCalled = false
        var dateFetched: LocalDate? = null
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                getEntryWasCalled = true
                dateFetched = date
                return Entry(date, "hii there")
            }

            override fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun addEntry(entry: Entry): Int = -1

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override suspend fun search(query: String): List<Entry> = emptyList()
        }

        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        assertThat(getEntryWasCalled).isTrue()
        assertThat(dateFetched).isEqualTo(LocalDate.of(2023, 10, 12))
        assertThat(viewModel.state.value.date).isEqualTo(LocalDate.of(2023, 10, 12))
        assertThat(viewModel.state.value.content).isEqualTo("hii there")
        assertThat(viewModel.state.value.isEditingExistingEntry).isTrue()
    }

    @Test
    fun `GIVEN an entry view model WHEN the view model is created THEN the quote settings are fetched`() {
        var shouldShowQuoteWasCalled = false
        val settings = object : PresentlySettings {
            override fun getCurrentTheme(): String = fail("Not needed in this test")

            override fun setTheme(themeName: String) = fail("Not needed in this test")

            override fun isBiometricsEnabled(): Boolean = fail("Not needed in this test")

            override fun shouldLockApp(): Boolean = fail("Not needed in this test")

            override fun onAppBackgrounded() = fail("Not needed in this test")

            override fun onAuthenticationSucceeded() = fail("Not needed in this test")

            override fun setOnPauseTime() = fail("Not needed in this test")

            override fun getFirstDayOfWeek(): Int = fail("Not needed in this test")

            override fun shouldShowQuote(): Boolean {
                shouldShowQuoteWasCalled = true
                return true
            }

            override fun getAutomaticBackupCadence(): BackupCadence =
                fail("Not needed in this test")

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
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        assertThat(shouldShowQuoteWasCalled).isTrue()
        assertThat(viewModel.state.value.shouldShowQuote).isTrue()
    }

    @Test
    fun `GIVEN an entry view model WHEN a user types THEN the text is saved after being debounced`() = runTest {
        var timesAddEntryWasCalled = 0
        var writtenEntry: Entry? = null
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return Entry(date, "hii there")
            }

            override fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun addEntry(entry: Entry): Int {
                timesAddEntryWasCalled++
                writtenEntry = entry
                return -1
            }

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override suspend fun search(query: String): List<Entry> = emptyList()
        }

        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("Hello this is new text!")
        delay(301L)

        assertThat(timesAddEntryWasCalled).isEqualTo(1)
        assertThat(writtenEntry).isEqualTo(
            Entry(
                LocalDate.of(2023, 10, 12),
                "Hello this is new text!"
            )
        )
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN logScreenView is called THEN log an analytics event`() {
        var viewedScreen = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {}

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {
                viewedScreen = viewName
            }

            override fun optOutOfAnalytics() {}

            override fun optIntoAnalytics() {}
        }
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.logScreenView()

        assertThat(viewedScreen).isEqualTo("Entry")
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onFabClicked is called THEN log an analytics event`() {
        var recordedEvent = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordedEvent = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {}

            override fun optOutOfAnalytics() {}

            override fun optIntoAnalytics() {}
        }
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onFabClicked()

        assertThat(recordedEvent).isEqualTo("EditButtonClicked")
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onFabClicked is called THEN update the state`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onFabClicked()

        assertThat(viewModel.state.value.isInEditMode).isTrue()
    }

    @Test
    fun `GIVEN entry view model WHEN onTextChanged is called THEN the state is updated`() = runTest {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)
        viewModel.onTextChanged("new text")
        delay(301L)

        val state = viewModel.state.value
        assertThat(state.content).isEqualTo("new text")
        assertThat(state.undoStack).contains("hii there")
        assertThat(state.redoStack).isEmpty()
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onExitEditMode is called AND the user has edited the existing entry THEN log an analytics event`() = runTest {
        var recordedEvent = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordedEvent = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {}

            override fun recordView(viewName: String) {}

            override fun optOutOfAnalytics() {}

            override fun optIntoAnalytics() {}
        }
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry {
                return Entry(date, "hii there")
            }

            override fun getEntriesFlow(): Flow<List<Entry>> {
                return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
            }

            override suspend fun getEntries(): List<Entry> {
                return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
            }

            override suspend fun addEntry(entry: Entry): Int = -1

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override suspend fun search(query: String): List<Entry> = emptyList()
        }
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("user made an edit")
        delay(301L)
        viewModel.onExitEditMode()

        assertThat(recordedEvent).isEqualTo("editedExistingEntry")
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onExitEditMode is called AND the user has written a new entry THEN log an analytics event`() = runTest {
        var actualNumberOfEntriesRecorded = -1
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {}

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int) {
                actualNumberOfEntriesRecorded = numEntries
            }

            override fun recordView(viewName: String) {}

            override fun optOutOfAnalytics() {}

            override fun optIntoAnalytics() {}
        }
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return null
            }

            override fun getEntriesFlow(): Flow<List<Entry>> {
                return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
            }

            override suspend fun getEntries(): List<Entry> {
                return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
            }

            override suspend fun addEntry(entry: Entry): Int = 199

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override suspend fun search(query: String): List<Entry> = emptyList()
        }
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("user made an edit")
        delay(301L)
        viewModel.onExitEditMode()

        assertThat(actualNumberOfEntriesRecorded).isEqualTo(199)
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onExitEditMode is called THEN update the state`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"

        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onFabClicked()
        viewModel.onExitEditMode()

        assertThat(viewModel.state.value.isInEditMode).isFalse()
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onExitEditMode is called AND the user just wrote a milestone entry THEN update the state`() = runTest {
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                return null //new entry
            }

            override fun getEntriesFlow(): Flow<List<Entry>> {
                return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
            }

            override suspend fun getEntries(): List<Entry> {
                return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
            }

            override suspend fun addEntry(entry: Entry): Int = 100

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override suspend fun search(query: String): List<Entry> = emptyList()
        }
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("user made an edit")
        delay(301L)
        viewModel.onExitEditMode()

        assertThat(viewModel.state.value.isInEditMode).isFalse()
        assertThat(viewModel.state.value.shouldShowMilestoneDialog).isTrue()
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onUndoClicked is called THEN update the state`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("user made an edit")
        viewModel.onTextChanged("user made an edit and then typed more")
        viewModel.onUndoClicked()

        assertThat(viewModel.state.value.redoStack).contains("user made an edit and then typed more")
        assertThat(viewModel.state.value.undoStack).doesNotContain("user made an edit and then typed more")
        assertThat(viewModel.state.value.content).isEqualTo("user made an edit")
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onRedoClicked is called THEN update the state`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onTextChanged("user made an edit")
        viewModel.onTextChanged("user made an edit and then typed more")
        viewModel.onUndoClicked()
        viewModel.onRedoClicked()

        assertThat(viewModel.state.value.redoStack).doesNotContain("user made an edit and then typed more")
        assertThat(viewModel.state.value.undoStack).contains("user made an edit")
        assertThat(viewModel.state.value.content).isEqualTo("user made an edit and then typed more")
    }

    @Test
    fun `GIVEN an EntryViewModel WHEN onDismissMilestoneDialog is called THEN update the state`() {
        val savedStateHandle = SavedStateHandle()
        savedStateHandle["entry-date"] = "2023-10-12"
        viewModel = EntryViewModel(savedStateHandle, repository, analytics, settings)

        viewModel.onDismissMilestoneDialog()

        assertThat(viewModel.state.value.shouldShowMilestoneDialog).isFalse()
    }
}
