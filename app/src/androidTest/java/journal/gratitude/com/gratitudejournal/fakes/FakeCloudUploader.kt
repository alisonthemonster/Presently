package journal.gratitude.com.gratitudejournal.fakes

import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import java.io.File

class FakeCloudUploader :
    CloudProvider {
    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return UploadSuccess
    }
}