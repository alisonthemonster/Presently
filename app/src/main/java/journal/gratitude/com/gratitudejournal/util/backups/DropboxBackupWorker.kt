package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader

@HiltWorker
class DropboxBackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val runner: BackupWorkerRunner,
    private val dropboxUploader: DropboxUploader
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return runner.run(applicationContext, dropboxUploader)
    }
}
