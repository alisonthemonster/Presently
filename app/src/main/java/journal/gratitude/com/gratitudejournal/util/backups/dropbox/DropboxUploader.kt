package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.content.Context
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.presently.backups.DropboxWebService
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.CloudProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException

class DropboxUploader(
    val dropboxWebService: DropboxWebService
): CloudProvider {

    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
                dropboxWebService.uploadFile(requestBody, "{\"path\": \"/PresentlyBackup.csv\",\"mode\": \"overwrite\",\"autorename\": true,\"mute\": false,\"strict_conflict\": false}")
                UploadSuccess
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

        suspend fun deauthorizeDropboxAccess(context: Context, settings: PresentlySettings) {
            withContext(Dispatchers.IO) {
                val accessToken = settings.getAccessToken()
                val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                    .build()
                val client = DbxClientV2(requestConfig, accessToken)
                client.auth().tokenRevoke()

                settings.clearAccessToken()
                WorkManager.getInstance(context).cancelAllWorkByTag(PRESENTLY_BACKUP)
            }
        }

        const val PRESENTLY_BACKUP = "PRESENTLY_BACKUP"
    }

}