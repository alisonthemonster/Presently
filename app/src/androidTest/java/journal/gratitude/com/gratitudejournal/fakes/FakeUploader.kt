package journal.gratitude.com.gratitudejournal.fakes

import android.content.Context
import androidx.work.ListenableWorker
import journal.gratitude.com.gratitudejournal.util.backups.Uploader
import javax.inject.Inject

class FakeUploader @Inject constructor() : Uploader {
    override suspend fun uploadEntries(appContext: Context): ListenableWorker.Result {
        return ListenableWorker.Result.success()
    }
}
