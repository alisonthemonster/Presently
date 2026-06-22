package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
class EntryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TestEntryRepository
    private lateinit var analytics: TestAnalyticsLogger
    private lateinit var settings: TestPresentlySettings

    @Before
    fun setUp() {
        repository = TestEntryRepository()
        analytics = TestAnalyticsLogger()
        settings = TestPresentlySettings()
    }

    @Test
    fun init_loadsExistingEntry() = runTest {
        val date = LocalDate.of(2021, 2, 28)
        repository.entryToReturn = Entry(date, "Saved entry")

        val viewModel = createViewModel(
            args = EntryArgs(
                date = date.toString(),
                isNewEntry = false,
                numberExistingEntries = 3,
                quote = "Quote",
                firstHint = "Hint",
                prompts = listOf("one", "two")
            )
        )
        advanceUntilIdle()

        assertThat(viewModel.state.value.entryContent).isEqualTo("Saved entry")
        assertThat(viewModel.state.value.hasUnsavedChanges).isFalse()
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun onPromptClicked_rotatesPromptAndRecordsAnalytics() = runTest {
        val viewModel = createViewModel(
            args = newEntryArgs(prompts = listOf("one", "two"))
        )
        advanceUntilIdle()

        viewModel.onPromptClicked()

        assertThat(viewModel.state.value.promptNumber).isEqualTo(1)
        assertThat(viewModel.state.value.hint).isEqualTo("two")
        assertThat(analytics.recordedEvents).contains("clickedNewPrompt")
    }

    @Test
    fun onTextChanged_tracksUnsavedChangesAgainstOriginalEntry() = runTest {
        val viewModel = createViewModel(args = newEntryArgs())
        advanceUntilIdle()

        viewModel.onTextChanged("new text")
        assertThat(viewModel.state.value.hasUnsavedChanges).isTrue()

        viewModel.onTextChanged("")
        assertThat(viewModel.state.value.hasUnsavedChanges).isFalse()
    }

    @Test
    fun onScreenShown_recordsViewOnlyOnce() = runTest {
        val viewModel = createViewModel(args = newEntryArgs())
        advanceUntilIdle()

        viewModel.onScreenShown()
        viewModel.onScreenShown()

        assertThat(analytics.recordedViews).containsExactly("EntryFragment")
    }

    @Test
    fun onShareClicked_emitsShareEffect() = runTest {
        val date = LocalDate.of(2021, 2, 28)
        val viewModel = createViewModel(args = newEntryArgs(date = date))
        advanceUntilIdle()
        viewModel.onTextChanged("Share me")

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onShareClicked()

        assertThat(effect.await()).isEqualTo(
            EntryEffect.OpenShare("Share me", date)
        )
        assertThat(analytics.recordedEvents).contains("sharedEntry")
    }

    @Test
    fun onQuoteLongClicked_emitsCopyEffect() = runTest {
        val viewModel = createViewModel(args = newEntryArgs())
        advanceUntilIdle()

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onQuoteLongClicked()

        assertThat(effect.await()).isEqualTo(EntryEffect.CopyQuote("Quote"))
        assertThat(analytics.recordedEvents).contains("copiedQuote")
    }

    @Test
    fun onBackPressed_withUnsavedChanges_showsDialogEffect() = runTest {
        val viewModel = createViewModel(args = newEntryArgs())
        advanceUntilIdle()
        viewModel.onTextChanged("Changed")

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onBackPressed()

        assertThat(effect.await()).isEqualTo(EntryEffect.ShowUnsavedChangesDialog)
    }

    @Test
    fun onBackPressed_withoutUnsavedChanges_navigatesBack() = runTest {
        val viewModel = createViewModel(args = newEntryArgs())
        advanceUntilIdle()

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onBackPressed()

        assertThat(effect.await()).isEqualTo(EntryEffect.NavigateBack)
    }

    @Test
    fun onDiscardChangesConfirmed_navigatesBack() = runTest {
        val viewModel = createViewModel(args = existingEntryArgs())
        advanceUntilIdle()

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onDiscardChangesConfirmed()

        assertThat(effect.await()).isEqualTo(EntryEffect.NavigateBack)
    }

    @Test
    fun saveEntry_forNewEntry_persistsAndEmitsSavedEffect() = runTest {
        val viewModel = createViewModel(args = newEntryArgs(numberExistingEntries = 0))
        advanceUntilIdle()
        viewModel.onTextChanged("Saved text")

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.saveEntry()
        advanceUntilIdle()

        assertThat(repository.addedEntries).containsExactly(
            Entry(LocalDate.now(), "Saved text")
        )
        assertThat(effect.await()).isEqualTo(
            EntryEffect.EntrySaved(milestoneNumber = 0, shouldTriggerReminderOnboarding = true)
        )
        assertThat(analytics.recordedEntryTotals).containsExactly(1)
        assertThat(viewModel.state.value.hasUnsavedChanges).isFalse()
    }

    @Test
    fun saveEntry_forMilestoneEntry_emitsMilestoneEffect() = runTest {
        val viewModel = createViewModel(args = newEntryArgs(numberExistingEntries = 4))
        advanceUntilIdle()
        viewModel.onTextChanged("Saved text")

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.saveEntry()
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(
            EntryEffect.EntrySaved(milestoneNumber = 5, shouldTriggerReminderOnboarding = false)
        )
    }

    @Test
    fun saveEntry_forExistingEntry_recordsEditEvent() = runTest {
        val viewModel = createViewModel(args = existingEntryArgs())
        advanceUntilIdle()
        viewModel.onTextChanged("Updated")

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.saveEntry()
        advanceUntilIdle()

        assertThat(effect.await()).isEqualTo(
            EntryEffect.EntrySaved(milestoneNumber = 0, shouldTriggerReminderOnboarding = false)
        )
        assertThat(analytics.recordedEvents).contains("editedExistingEntry")
    }

    private fun createViewModel(args: EntryArgs): EntryViewModel {
        return EntryViewModel(
            savedStateHandle = SavedStateHandle(mapOf(EntryFragment.ENTRY_ARGS_KEY to args)),
            analytics = analytics,
            repository = repository,
            settings = settings
        )
    }

    private fun newEntryArgs(
        date: LocalDate = LocalDate.now(),
        numberExistingEntries: Int = 0,
        prompts: List<String> = listOf("one", "two")
    ) = EntryArgs(
        date = date.toString(),
        isNewEntry = true,
        numberExistingEntries = numberExistingEntries,
        quote = "Quote",
        firstHint = "Hint",
        prompts = prompts
    )

    private fun existingEntryArgs(
        date: LocalDate = LocalDate.now()
    ) = EntryArgs(
        date = date.toString(),
        isNewEntry = false,
        numberExistingEntries = 3,
        quote = "Quote",
        firstHint = "Hint",
        prompts = listOf("one", "two")
    )
}

private class TestEntryRepository : EntryRepository {
    var entryToReturn: Entry? = null
    val addedEntries = mutableListOf<Entry>()

    override suspend fun getEntry(date: LocalDate): Entry? = entryToReturn

    override suspend fun getRandomEntry(): Entry? = null

    override suspend fun getEntriesFlow(): Flow<List<Entry>> = flowOf(emptyList())

    override suspend fun getEntries(): List<Entry> = emptyList()

    override fun getWrittenDates(): LiveData<List<LocalDate>> = MutableLiveData(emptyList())

    override suspend fun addEntry(entry: Entry) {
        addedEntries += entry
    }

    override suspend fun addEntries(entries: List<Entry>) = Unit

    override fun searchEntries(query: String): Flow<PagingData<Entry>> = emptyFlow()
}

private class TestAnalyticsLogger : AnalyticsLogger {
    val recordedEvents = mutableListOf<String>()
    val recordedViews = mutableListOf<String>()
    val recordedEntryTotals = mutableListOf<Int>()

    override fun recordEvent(event: String) {
        recordedEvents += event
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        recordedEvents += event
    }

    override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit

    override fun recordEntryAdded(numEntries: Int) {
        recordedEntryTotals += numEntries
    }

    override fun recordView(viewName: String) {
        recordedViews += viewName
    }

    override fun optOutOfAnalytics() = Unit

    override fun optIntoAnalytics() = Unit
}

private class TestPresentlySettings : PresentlySettings {
    override fun getCurrentTheme(): String = ""
    override fun setTheme(themeName: String) = Unit
    override fun isBiometricsEnabled(): Boolean = false
    override fun shouldLockApp(): Boolean = false
    override fun forceLock() = Unit
    override fun setOnPauseTime() = Unit
    override fun getFirstDayOfWeek(): Int = 1
    override fun shouldShowQuote(): Boolean = true
    override fun getAutomaticBackupCadence(): BackupCadence = BackupCadence.DAILY
    override fun getLocale(): String = "en"
    override fun hasEnabledNotifications(): Boolean = false
    override fun setNotificationsEnabled(enabled: Boolean) = Unit
    override fun getNotificationTime(): LocalTime = LocalTime.NOON
    override fun setNotificationTime(time: LocalTime) = Unit
    override fun hasUserDisabledAlarmReminders(context: android.content.Context): Boolean = false
    override fun hasSeenReminderOnboarding(): Boolean = false
    override fun markReminderOnboardingSeen() = Unit
    override fun clearReminderOnboardingSeen() = Unit
    override fun hasRequestedNotificationPermission(): Boolean = false
    override fun markNotificationPermissionRequested() = Unit
    override fun getLinesPerEntryInTimeline(): Int = 3
    override fun shouldShowDayOfWeekInTimeline(): Boolean = false
    override fun getAccessToken(): com.dropbox.core.oauth.DbxCredential? = null
    override fun setAccessToken(newToken: com.dropbox.core.oauth.DbxCredential) = Unit
    override fun wasDropboxAuthInitiated(): Boolean = false
    override fun markDropboxAuthAsCancelled() = Unit
    override fun markDropboxAuthInitiated() = Unit
    override fun clearAccessToken() = Unit
    override fun isOptedIntoAnalytics(): Boolean = true
}
