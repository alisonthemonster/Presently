package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import com.dropbox.core.DbxException
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import journal.gratitude.com.gratitudejournal.BuildConfig
import journal.gratitude.com.gratitudejournal.model.CloudUploadResult
import journal.gratitude.com.gratitudejournal.model.UploadError
import journal.gratitude.com.gratitudejournal.model.UploadSuccess
import java.io.File
import java.io.FileInputStream
import java.io.IOException


class DropboxUploader(val client: DbxClientV2): CloudProvider {

    override suspend fun uploadToCloud(file: File): CloudUploadResult {
        try {
            FileInputStream(file).use { inputStream ->
                client.files().uploadBuilder("/backup.csv")
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream)
                return UploadSuccess
            }
        } catch (e: DbxException) {
            return UploadError(e)
        } catch (e: IOException) {
            return UploadError(e)
        }
    }

    companion object {

        fun authorizeDropboxAccess(context: Context) {
            Auth.startOAuth2Authentication(context, BuildConfig.DROPBOX_APP_KEY)
        }
    }

}