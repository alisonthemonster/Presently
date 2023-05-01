package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class UploadToCloudWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val uploader: Uploader
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return uploader.uploadEntries(appContext)
    }
}
