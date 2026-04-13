package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider

@HiltWorker
class GoogleDriveBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val runner: BackupWorkerRunner,
    private val googleDriveBackupProvider: GoogleDriveBackupProvider
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runner.run(applicationContext, googleDriveBackupProvider)
    }
}
