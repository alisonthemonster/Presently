package journal.gratitude.com.gratitudejournal.fakes

import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import java.io.File
import javax.inject.Inject

class FakeCloudUploader @Inject constructor():
    CloudProvider {
    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return UploadSuccess
    }
}