package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import journal.gratitude.com.gratitudejournal.util.backups.BackupRestoreManager
import journal.gratitude.com.gratitudejournal.util.backups.BackupWorkScheduler
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.threeten.bp.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BackupSettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val analytics = object : AnalyticsLogger {
        override fun recordEvent(event: String) = Unit
        override fun recordEvent(event: String, details: Map<String, Any>) = Unit
        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
        override fun recordEntryAdded(numEntries: Int) = Unit
        override fun recordView(viewName: String) = Unit
        override fun optOutOfAnalytics() = Unit
        override fun optIntoAnalytics() = Unit
    }
    private val crashReporter = object : CrashReporter {
        override fun logHandledException(exception: Exception) = Unit
        override fun optOutOfCrashReporting() = Unit
        override fun optIntoCrashReporting() = Unit
    }
    private lateinit var viewModel: BackupSettingsViewModel
    private lateinit var backupPreferences: BackupPreferences

    @Before
    fun setUp() {
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().build()
        )
        backupPreferences = BackupPreferences(
            PreferenceManager.getDefaultSharedPreferences(context),
            analytics
        )

        val repository: EntryRepository = FakeEntryRepository()
        val backupWorkScheduler = BackupWorkScheduler(context, backupPreferences)
        val googleDriveBackupProvider = GoogleDriveBackupProvider(context, backupPreferences)
        val backupRestoreManager = BackupRestoreManager(repository, googleDriveBackupProvider)
        val dropboxUploader = DropboxUploader(context, backupPreferences)

        viewModel = BackupSettingsViewModel(
            repository = repository,
            backupPreferences = backupPreferences,
            backupWorkScheduler = backupWorkScheduler,
            backupRestoreManager = backupRestoreManager,
            dropboxUploader = dropboxUploader,
            googleDriveBackupProvider = googleDriveBackupProvider,
            analytics = analytics,
            crashReporter = crashReporter
        )
    }

    @Test
    fun onImportClicked_showsImportWarning() {
        viewModel.onImportClicked()

        assertThat(viewModel.state.value.showImportWarning).isTrue()
    }

    @Test
    fun onImportConfirmed_hidesWarning() {
        viewModel.onImportClicked()

        viewModel.onImportConfirmed()

        assertThat(viewModel.state.value.showImportWarning).isFalse()
    }

    @Test
    fun onDropboxFrequencySelected_updatesUiState() = runTest {
        viewModel.onDropboxFrequencySelected(BackupFrequency.MONTHLY)
        advanceUntilIdle()

        assertThat(viewModel.state.value.dropbox.frequency).isEqualTo(BackupFrequency.MONTHLY)
        assertThat(backupPreferences.getDropboxState().frequency).isEqualTo(BackupFrequency.MONTHLY)
    }

    @Test
    fun onGoogleDriveFrequencySelected_updatesUiState() = runTest {
        viewModel.onGoogleDriveFrequencySelected(BackupFrequency.ON_EVERY_CHANGE)
        advanceUntilIdle()

        assertThat(viewModel.state.value.googleDrive.frequency)
            .isEqualTo(BackupFrequency.ON_EVERY_CHANGE)
        assertThat(backupPreferences.getGoogleDriveState().frequency)
            .isEqualTo(BackupFrequency.ON_EVERY_CHANGE)
    }
}

private class FakeEntryRepository : EntryRepository {
    override suspend fun getEntry(date: LocalDate): Entry? = null

    override suspend fun getEntriesFlow(): Flow<List<Entry>> {
        throw NotImplementedError()
    }

    override suspend fun getEntries(): List<Entry> = emptyList()

    override fun getWrittenDates(): LiveData<List<LocalDate>> {
        throw NotImplementedError()
    }

    override suspend fun addEntry(entry: Entry) = Unit

    override suspend fun addEntries(entries: List<Entry>) = Unit

    override fun searchEntries(query: String): Flow<PagingData<Entry>> {
        throw NotImplementedError()
    }
}
