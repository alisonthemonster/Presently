package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupFile
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupRestoreManager @Inject constructor(
    private val repository: EntryRepository,
    private val googleDriveBackupProvider: GoogleDriveBackupProvider
) {

    suspend fun loadGoogleDriveRestorePreview(accountEmail: String): RestorePreview? {
        val backupFile = googleDriveBackupProvider.getBackupForRestore(accountEmail) ?: return null
        val localEntries = repository.getEntries()
        return RestorePreview(
            backupFile = backupFile,
            existingEntryCount = localEntries.size
        )
    }

    suspend fun restoreFromGoogleDrive(backupFile: GoogleDriveBackupFile): RestoreResult {
        val existingDates = repository.getEntries()
            .mapTo(linkedSetOf()) { it.entryDate }
        val entriesToImport = backupFile.entries.filter { existingDates.add(it.entryDate) }
        repository.addEntries(entriesToImport)
        return RestoreResult(
            importedEntries = entriesToImport,
            skippedEntryCount = backupFile.entries.size - entriesToImport.size
        )
    }
}

data class RestorePreview(
    val backupFile: GoogleDriveBackupFile,
    val existingEntryCount: Int
)

data class RestoreResult(
    val importedEntries: List<Entry>,
    val skippedEntryCount: Int
)
