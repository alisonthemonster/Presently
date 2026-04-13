package journal.gratitude.com.gratitudejournal.util.backups.dropbox

import android.content.Context
import androidx.work.WorkManager
import com.dropbox.core.DbxException
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import journal.gratitude.com.gratitudejournal.BuildConfig
import com.dropbox.core.v2.files.UploadErrorException
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import journal.gratitude.com.gratitudejournal.util.backups.BackupAuthFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupStorageFullFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadResult
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.CloudBackupProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DropboxUploader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupPreferences: BackupPreferences
) : CloudBackupProvider {

    override val provider: BackupProvider = BackupProvider.DROPBOX

    override suspend fun uploadToCloud(file: File): BackupUploadResult {
        return withContext(Dispatchers.IO) {
            val accessToken = backupPreferences.getDropboxCredential()
                ?: return@withContext BackupAuthFailure(
                    IllegalStateException("Dropbox credential missing during backup")
                )
            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                .build()

            val client = DbxClientV2(requestConfig, accessToken)

            try {
                FileInputStream(file).use { inputStream ->
                    client.files().uploadBuilder("/presently-backup.csv")
                        .withMode(WriteMode.OVERWRITE)
                        .uploadAndFinish(inputStream)
                    BackupUploadSuccess
                }
            } catch (e: InvalidAccessTokenException) {
                BackupAuthFailure(e)
            } catch (e: UploadErrorException) {
                if (e.userMessage.text.contains("insufficient_space")) {
                    BackupStorageFullFailure(e)
                } else {
                    BackupUploadFailure(e)
                }
            } catch (e: DbxException) {
                BackupUploadFailure(e)
            } catch (e: IOException) {
                BackupUploadFailure(e)
            }
        }
    }

    suspend fun fetchCurrentAccountEmail(): String? {
        return withContext(Dispatchers.IO) {
            val accessToken = backupPreferences.getDropboxCredential() ?: return@withContext null
            val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid").build()
            runCatching {
                DbxClientV2(requestConfig, accessToken).users().currentAccount.email
            }.getOrNull()
        }
    }

    override suspend fun disconnect() {
        deauthorizeDropboxAccess(context, backupPreferences)
    }

    companion object {

        fun authorizeDropboxAccess(context: Context, backupPreferences: BackupPreferences) {
            backupPreferences.markDropboxAuthInitiated()

            val clientIdentifier = "PresentlyAndroid/${BuildConfig.VERSION_NAME}"
            val requestConfig = DbxRequestConfig(clientIdentifier)
            Auth.startOAuth2PKCE(context, BuildConfig.DROPBOX_APP_KEY, requestConfig)
        }

        suspend fun deauthorizeDropboxAccess(
            context: Context,
            backupPreferences: BackupPreferences,
            dispatcher: CoroutineDispatcher = Dispatchers.IO
        ) {
            withContext(dispatcher) {
                val accessToken = backupPreferences.getDropboxCredential()
                if (accessToken != null) {
                    val requestConfig = DbxRequestConfig.newBuilder("PresentlyAndroid")
                        .build()
                    runCatching {
                        DbxClientV2(requestConfig, accessToken).auth().tokenRevoke()
                    }
                }

                backupPreferences.clearDropboxConnection()
                WorkManager.getInstance(context).cancelAllWorkByTag(BackupProvider.DROPBOX.workerTag)
            }
        }
    }
}
