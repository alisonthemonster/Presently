package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.NetworkIOException
import com.dropbox.core.ServerException
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.auth.AuthError.INVALID_ACCESS_TOKEN
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(application = DropboxUploaderTest.TestApplication::class)
class DropboxUploaderTest {

    class TestApplication : Application(), Configuration.Provider {
        override val workManagerConfiguration: Configuration
            get() = Configuration.Builder().build()
    }

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val credential = DbxCredential("token")

    private fun settingsWithToken(): PresentlySettings = mock {
        on { getAccessToken() } doReturn credential
    }

    @Test
    fun `GIVEN revoke succeeds WHEN deauthorizing THEN the token is cleared`() = runTest {
        val settings = settingsWithToken()
        var revoked: DbxCredential? = null

        DropboxUploader.deauthorizeDropboxAccess(
            context, settings, StandardTestDispatcher(testScheduler)
        ) { revoked = it }

        assertThat(revoked).isEqualTo(credential)
        verify(settings).clearAccessToken()
    }

    @Test
    fun `GIVEN the token is already invalid WHEN deauthorizing THEN the token is still cleared`() = runTest {
        val settings = settingsWithToken()

        DropboxUploader.deauthorizeDropboxAccess(
            context, settings, StandardTestDispatcher(testScheduler)
        ) { throw InvalidAccessTokenException("requestId", "message", INVALID_ACCESS_TOKEN) }

        verify(settings).clearAccessToken()
    }

    @Test
    fun `GIVEN the device is offline WHEN deauthorizing THEN it does not throw and clears the token`() = runTest {
        val settings = settingsWithToken()

        DropboxUploader.deauthorizeDropboxAccess(
            context, settings, StandardTestDispatcher(testScheduler)
        ) { throw NetworkIOException(IOException("offline")) }

        verify(settings).clearAccessToken()
    }

    @Test
    fun `GIVEN an expected revoke failure WHEN deauthorizing THEN it is not reported`() = runTest {
        val reported = mutableListOf<Exception>()

        listOf(
            InvalidAccessTokenException("requestId", "message", INVALID_ACCESS_TOKEN),
            NetworkIOException(IOException("offline"))
        ).forEach { expected ->
            DropboxUploader.deauthorizeDropboxAccess(
                context,
                settingsWithToken(),
                StandardTestDispatcher(testScheduler),
                revokeToken = { throw expected },
                onRevokeFailure = { reported += it }
            )
        }

        assertThat(reported).isEmpty()
    }

    @Test
    fun `GIVEN an unexpected revoke failure WHEN deauthorizing THEN it is reported and the token cleared`() = runTest {
        val settings = settingsWithToken()
        val failure = ServerException("requestId", "boom")
        val reported = mutableListOf<Exception>()

        DropboxUploader.deauthorizeDropboxAccess(
            context,
            settings,
            StandardTestDispatcher(testScheduler),
            revokeToken = { throw failure },
            onRevokeFailure = { reported += it }
        )

        assertThat(reported).containsExactly(failure)
        verify(settings).clearAccessToken()
    }

    @Test
    fun `GIVEN no token WHEN deauthorizing THEN nothing is revoked but state is cleared`() = runTest {
        val settings = mock<PresentlySettings> { on { getAccessToken() } doReturn null }
        var revokeCalled = false

        DropboxUploader.deauthorizeDropboxAccess(
            context, settings, StandardTestDispatcher(testScheduler)
        ) { revokeCalled = true }

        assertThat(revokeCalled).isFalse()
        verify(settings).clearAccessToken()
    }
}
