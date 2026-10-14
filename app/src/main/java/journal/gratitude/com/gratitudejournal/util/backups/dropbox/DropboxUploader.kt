package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.content.Context
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.NetworkIOException
import com.dropbox.core.android.Auth
import com.dropbox.core.oauth.DbxCredential
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface CloudProvider {
    suspend fun uploadToCloud(file: File): CloudUploadResult
}

class DropboxUploader(val context: Context, val settings: PresentlySettings):
    CloudProvider {

    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return withContext(Dispatchers.IO) {
            val accessToken = settings.getAccessToken()
            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                .build()

            val client = DbxClientV2(requestConfig, accessToken)

            try {
                FileInputStream(file).use { inputStream ->
                    client.files().uploadBuilder("/presently-backup.csv")
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream)
                    UploadSuccess
                }
            } catch (e: DbxException) {
                UploadError(e)
            } catch (e: IOException) {
                UploadError(e)
            }
        }
    }

    companion object {

        fun authorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            settings.markDropboxAuthInitiated()

            val clientIdentifier = "PresentlyAndroid/${BuildConfig.VERSION_NAME}"
            val requestConfig = DbxRequestConfig(clientIdentifier)
            Auth.startOAuth2PKCE(context, BuildConfig.DROPBOX_APP_KEY, requestConfig)
        }

        /**
         * Disconnects Dropbox locally. Revoking the token with Dropbox is best effort: the token
         * may already be invalid (that is often why we're here) or the device may be offline, and
         * neither should stop the token being cleared and scheduled backups being cancelled.
         * Those two expected cases are ignored; anything else is passed to [onRevokeFailure].
         */
        suspend fun deauthorizeDropboxAccess(
            context: Context,
            settings: PresentlySettings,
            dispatcher: CoroutineDispatcher = Dispatchers.IO,
            onRevokeFailure: (DbxException) -> Unit = {},
            revokeToken: (DbxCredential) -> Unit = ::revokeWithDropbox
        ) {
            withContext(dispatcher) {
                val accessToken = settings.getAccessToken()
                if (accessToken != null) {
                    try {
                        revokeToken(accessToken)
                    } catch (e: InvalidAccessTokenException) {
                        // already invalid, nothing left to revoke
                    } catch (e: NetworkIOException) {
                        // offline, nothing we can do about it
                    } catch (e: DbxException) {
                        onRevokeFailure(e)
                    }
                }

                settings.clearAccessToken()
                WorkManager.getInstance(context).cancelAllWorkByTag(PRESENTLY_BACKUP)
            }
        }

        private fun revokeWithDropbox(credential: DbxCredential) {
            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid").build()
            DbxClientV2(requestConfig, credential).auth().tokenRevoke()
        }

        const val PRESENTLY_BACKUP = "PRESENTLY_BACKUP"
    }

}