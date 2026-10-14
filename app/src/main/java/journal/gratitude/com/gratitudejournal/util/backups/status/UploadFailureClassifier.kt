package journal.gratitude.com.gratitudejournal.util.backups.status

import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.NetworkIOException
import com.dropbox.core.RetryException
import com.dropbox.core.ServerException
import com.dropbox.core.v2.files.UploadErrorException

object UploadFailureClassifier {

    fun classify(exception: Exception): UploadFailureType = when {
        exception is InvalidAccessTokenException -> UploadFailureType.AUTH
        exception.isInsufficientSpace() -> UploadFailureType.STORAGE_FULL
        exception is NetworkIOException -> UploadFailureType.NETWORK
        // RateLimitException extends RetryException
        exception is RetryException || exception is ServerException ->
            UploadFailureType.TRANSIENT_SERVER
        // Includes plain IOException, a local failure (e.g. reading the temp file), not the network.
        else -> UploadFailureType.OTHER
    }

    private fun Exception.isInsufficientSpace(): Boolean {
        if (this !is UploadErrorException) return false
        val typed = errorValue?.takeIf { it.isPath }?.pathValue?.reason?.isInsufficientSpace == true
        // Fallback for responses that only carry the message text.
        return typed || userMessage?.text?.contains("insufficient_space") == true
    }
}
