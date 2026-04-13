package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupFile
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.threeten.bp.LocalDate

@RunWith(RobolectricTestRunner::class)
class BackupRestoreManagerTest {

    private val repository = FakeEntryRepository()
    private lateinit var restoreManager: BackupRestoreManager

    @Before
    fun setUp() {
        repository.savedEntries.clear()
        repository.entriesToReturn = listOf(
            Entry(LocalDate.of(2024, 1, 1), "Local entry")
        )

        val context = ApplicationProvider.getApplicationContext<Context>()
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) = Unit
            override fun recordEvent(event: String, details: Map<String, Any>) = Unit
            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
            override fun recordEntryAdded(numEntries: Int) = Unit
            override fun recordView(viewName: String) = Unit
            override fun optOutOfAnalytics() = Unit
            override fun optIntoAnalytics() = Unit
        }
        val backupPreferences = BackupPreferences(
            PreferenceManager.getDefaultSharedPreferences(context),
            analytics
        )
        val googleDriveBackupProvider = GoogleDriveBackupProvider(context, backupPreferences, analytics)
        restoreManager = BackupRestoreManager(repository, googleDriveBackupProvider)
    }

    @Test
    fun restoreFromGoogleDrive_mergesAndDeduplicatesByDate() = runTest {
        val backupFile = GoogleDriveBackupFile(
            id = "backup-id",
            modifiedTime = "2026-04-12T10:15:37Z",
            entries = listOf(
                Entry(LocalDate.of(2024, 1, 1), "Remote duplicate"),
                Entry(LocalDate.of(2024, 1, 2), "Remote unique")
            )
        )

        val result = restoreManager.restoreFromGoogleDrive(backupFile)

        assertThat(result.importedEntries).containsExactly(
            Entry(LocalDate.of(2024, 1, 2), "Remote unique")
        )
        assertThat(result.skippedEntryCount).isEqualTo(1)
        assertThat(repository.savedEntries).containsExactly(
            Entry(LocalDate.of(2024, 1, 2), "Remote unique")
        )
    }
}

private class FakeEntryRepository : EntryRepository {
    var entriesToReturn: List<Entry> = emptyList()
    val savedEntries = mutableListOf<Entry>()

    override suspend fun getEntry(date: LocalDate): Entry? = entriesToReturn.find { it.entryDate == date }

    override suspend fun getEntriesFlow(): Flow<List<Entry>> {
        throw NotImplementedError()
    }

    override suspend fun getEntries(): List<Entry> = entriesToReturn

    override fun getWrittenDates(): LiveData<List<LocalDate>> {
        throw NotImplementedError()
    }

    override suspend fun addEntry(entry: Entry) {
        savedEntries += entry
    }

    override suspend fun addEntries(entries: List<Entry>) {
        savedEntries += entries
    }

    override fun searchEntries(query: String): Flow<PagingData<Entry>> {
        throw NotImplementedError()
    }
}
