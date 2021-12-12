package journal.gratitude.com.gratitudejournal.model

sealed class CloudUploadResult
object UploadSuccess : CloudUploadResult()
class UploadError(val exception: Exception, val message: String = exception.localizedMessage ?: "Unknown error") :
    CloudUploadResult()
