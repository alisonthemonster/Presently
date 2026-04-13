package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.work.ListenableWorker
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.model.CsvFileCreated
import journal.gratitude.com.gratitudejournal.model.CsvFileError
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.util.backups.FileExporter
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupWorkerRunner @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val repository: EntryRepository,
    private val crashReporter: CrashReporter,
    private val backupPreferences: BackupPreferences,
    private val failureNotifier: BackupFailureNotifier
) {

    suspend fun run(appContext: Context, provider: CloudBackupProvider): ListenableWorker.Result {
        val items = repository.getEntries()
        if (items.isEmpty()) {
            return ListenableWorker.Result.success()
        }

        val file = withContext(dispatchers.io) {
            File.createTempFile("tempPresentlyBackup", null, appContext.cacheDir)
        }

        return try {
            val exporter = FileExporter(FileWriter(file), dispatchers)
            when (val csvResult = exporter.exportToCSV(items, file)) {
                is CsvFileCreated -> handleUploadResult(
                    context = appContext,
                    provider = provider,
                    uploadResult = provider.uploadToCloud(csvResult.file)
                )
                is CsvFileError -> ListenableWorker.Result.failure()
            }
        } finally {
            file.delete()
        }
    }

    private suspend fun handleUploadResult(
        context: Context,
        provider: CloudBackupProvider,
        uploadResult: BackupUploadResult
    ): ListenableWorker.Result {
        return when (uploadResult) {
            BackupUploadSuccess -> {
                backupPreferences.updateLastBackupTimestamp(
                    provider.provider,
                    System.currentTimeMillis()
                )
                failureNotifier.cancel(provider.provider, context)
                ListenableWorker.Result.success()
            }
            is BackupAuthFailure -> {
                provider.disconnect()
                failureNotifier.notifyAuthFailure(context, provider.provider)
                crashReporter.logHandledException(uploadResult.exception)
                ListenableWorker.Result.failure()
            }
            is BackupStorageFullFailure -> {
                failureNotifier.notifyStorageFull(context, provider.provider)
                crashReporter.logHandledException(uploadResult.exception)
                ListenableWorker.Result.failure()
            }
            is BackupUploadFailure -> {
                crashReporter.logHandledException(uploadResult.exception)
                ListenableWorker.Result.failure()
            }
        }
    }
}
