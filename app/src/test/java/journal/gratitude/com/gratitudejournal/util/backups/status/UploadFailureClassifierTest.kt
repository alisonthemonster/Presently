package journal.gratitude.com.gratitudejournal.util.backups.status

import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.LocalizedText
import com.dropbox.core.NetworkIOException
import com.dropbox.core.RateLimitException
import com.dropbox.core.ServerException
import com.dropbox.core.DbxException
import com.dropbox.core.v2.files.UploadError
import com.dropbox.core.v2.files.UploadErrorException
import com.dropbox.core.v2.files.UploadWriteFailed
import com.dropbox.core.v2.files.WriteError
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.IOException

class UploadFailureClassifierTest {

    private fun uploadError(value: UploadError, text: String = "error") =
        UploadErrorException("/route", "requestId", LocalizedText(text, "en_US"), value)

    @Test
    fun `invalid token is auth`() {
        val e = InvalidAccessTokenException("id", "msg", null)
        assertThat(UploadFailureClassifier.classify(e)).isEqualTo(UploadFailureType.AUTH)
    }

    @Test
    fun `typed insufficient space is storage full`() {
        val value = UploadError.path(UploadWriteFailed(WriteError.INSUFFICIENT_SPACE, "session"))
        assertThat(UploadFailureClassifier.classify(uploadError(value)))
            .isEqualTo(UploadFailureType.STORAGE_FULL)
    }

    @Test
    fun `message-only insufficient space is storage full`() {
        val e = uploadError(UploadError.OTHER, "insufficient_space")
        assertThat(UploadFailureClassifier.classify(e)).isEqualTo(UploadFailureType.STORAGE_FULL)
    }

    @Test
    fun `other write error is other`() {
        val value = UploadError.path(UploadWriteFailed(WriteError.DISALLOWED_NAME, "session"))
        assertThat(UploadFailureClassifier.classify(uploadError(value)))
            .isEqualTo(UploadFailureType.OTHER)
    }

    @Test
    fun `network exception is network`() {
        val e = NetworkIOException(IOException("offline"))
        assertThat(UploadFailureClassifier.classify(e)).isEqualTo(UploadFailureType.NETWORK)
    }

    @Test
    fun `rate limit is transient server`() {
        val e = RateLimitException("id", null, 1, java.util.concurrent.TimeUnit.SECONDS)
        assertThat(UploadFailureClassifier.classify(e)).isEqualTo(UploadFailureType.TRANSIENT_SERVER)
    }

    @Test
    fun `server exception is transient server`() {
        val e = ServerException("id", "msg")
        assertThat(UploadFailureClassifier.classify(e)).isEqualTo(UploadFailureType.TRANSIENT_SERVER)
    }

    @Test
    fun `plain io exception is other`() {
        assertThat(UploadFailureClassifier.classify(IOException("disk")))
            .isEqualTo(UploadFailureType.OTHER)
    }
}
