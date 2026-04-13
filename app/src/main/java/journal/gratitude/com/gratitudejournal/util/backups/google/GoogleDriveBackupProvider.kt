package journal.gratitude.com.gratitudejournal.util.backups.google

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.RevokeAccessRequest
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.File as DriveFile
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import journal.gratitude.com.gratitudejournal.util.backups.BackupAuthFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupStorageFullFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadResult
import journal.gratitude.com.gratitudejournal.util.backups.BackupUploadSuccess
import journal.gratitude.com.gratitudejournal.util.backups.CloudBackupProvider
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter.convertCsvToEntries
import journal.gratitude.com.gratitudejournal.util.backups.RealCsvParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveBackupProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupPreferences: BackupPreferences
) : CloudBackupProvider {

    override val provider: BackupProvider = BackupProvider.GOOGLE_DRIVE
    private val authorizationClient = Identity.getAuthorizationClient(context)

    override suspend fun uploadToCloud(file: File): BackupUploadResult {
        return withContext(Dispatchers.IO) {
            val accountEmail = backupPreferences.getGoogleDriveState().accountEmail
                ?: run {
                    Log.e(TAG, "uploadToCloud: no account email stored, cannot upload")
                    return@withContext BackupAuthFailure(
                        IllegalStateException("Google Drive account missing during backup")
                    )
                }

            Log.d(TAG, "uploadToCloud: starting upload for account $accountEmail")
            try {
                val drive = buildDriveService(accountEmail)
                    ?: return@withContext BackupAuthFailure(
                        IOException("Failed to build Drive service")
                    )

                val csvBytes = file.readBytes()
                val existingFileId = findBackupFileId(drive)
                Log.d(TAG, "uploadToCloud: existingFileId=$existingFileId, csvBytes=${csvBytes.size}")

                if (existingFileId == null) {
                    createBackupFile(drive, csvBytes)
                } else {
                    updateBackupFile(drive, existingFileId, csvBytes)
                }

                Log.d(TAG, "uploadToCloud: backup uploaded successfully")
                BackupUploadSuccess
            } catch (exception: GoogleDriveQuotaException) {
                Log.e(TAG, "uploadToCloud: storage quota exceeded", exception)
                BackupStorageFullFailure(exception)
            } catch (exception: GoogleDriveAuthException) {
                Log.e(TAG, "uploadToCloud: auth exception", exception)
                BackupAuthFailure(exception)
            } catch (exception: IOException) {
                Log.e(TAG, "uploadToCloud: IO exception", exception)
                BackupUploadFailure(exception)
            }
        }
    }

    suspend fun getBackupForRestore(accountEmail: String): GoogleDriveBackupFile? {
        return withContext(Dispatchers.IO) {
            try {
                val drive = buildDriveService(accountEmail)
                    ?: return@withContext null

                Log.d(TAG, "getBackupForRestore: looking for backup file")
                val fileId = findBackupFileId(drive) ?: return@withContext null

                val contents = downloadBackup(drive, fileId)
                val parser = CSVParser.parse(
                    ByteArrayInputStream(contents),
                    StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT
                )
                val entries = convertCsvToEntries(RealCsvParser(parser))

                val metadata = drive.files().get(fileId).setFields("modifiedTime").execute()
                GoogleDriveBackupFile(
                    id = fileId,
                    modifiedTime = metadata.modifiedTime?.toString() ?: "",
                    entries = entries
                )
            } catch (e: Exception) {
                Log.e(TAG, "getBackupForRestore: failed", e)
                null
            }
        }
    }

    suspend fun beginInteractiveAuthorization(): AuthorizationResult {
        return authorizationClient
            .authorize(authorizationRequest(promptSelectAccount = true))
            .await()
    }

    fun getAuthorizationResultFromIntent(data: Intent?): AuthorizationResult {
        requireNotNull(data) {
            "Authorization intent was null"
        }
        return authorizationClient.getAuthorizationResultFromIntent(data)
    }

    suspend fun resolveAuthorizedAccountEmail(result: AuthorizationResult): String? {
        val accessToken = result.accessToken
        if (accessToken == null) {
            Log.w(TAG, "resolveAuthorizedAccountEmail: accessToken is null in AuthorizationResult (grantedScopes=${result.grantedScopes})")
            return null
        }
        Log.d(TAG, "resolveAuthorizedAccountEmail: access token obtained")
        // For the library approach, we just need to confirm we can access Drive API
        // The actual email retrieval happens when building the Drive service
        return null // Will be populated via other means
    }

    override suspend fun disconnect() {
        val accountEmail = backupPreferences.getGoogleDriveState().accountEmail
        if (accountEmail != null) {
            runCatching {
                authorizationClient.revokeAccess(
                    RevokeAccessRequest.builder()
                        .setAccount(Account(accountEmail, GOOGLE_ACCOUNT_TYPE))
                        .setScopes(requestedScopes())
                        .build()
                ).await()
            }
        } else {
            runCatching {
                authorizationClient.revokeAccess(
                    RevokeAccessRequest.builder().build()
                ).await()
            }
        }
        backupPreferences.clearGoogleDriveConnection()
        WorkManager.getInstance(context).cancelAllWorkByTag(BackupProvider.GOOGLE_DRIVE.workerTag)
    }

    private fun buildDriveService(accountEmail: String): Drive? {
        return try {
            Log.d(TAG, "buildDriveService: creating Drive service for $accountEmail")
            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                requestedScopes().map { it.scopeUri }
            )
            val account = android.accounts.Account(accountEmail, GOOGLE_ACCOUNT_TYPE)
            credential.selectedAccount = account

            Drive.Builder(
                NetHttpTransport(),
                GsonFactory(),
                credential
            ).setApplicationName("Presently").build()
        } catch (e: Exception) {
            Log.e(TAG, "buildDriveService: failed to build Drive service", e)
            null
        }
    }

    private fun findBackupFileId(drive: Drive): String? {
        return try {
            Log.d(TAG, "findBackupFileId: searching for backup file")
            val result = drive.files().list()
                .setSpaces("appDataFolder")
                .setFields("files(id, name)")
                .setQ("name='$BACKUP_FILE_NAME' and trashed=false")
                .setPageSize(1)
                .execute()

            val fileId = result.files?.firstOrNull()?.id
            Log.d(TAG, "findBackupFileId: found=${ fileId != null}")
            fileId
        } catch (e: Exception) {
            Log.e(TAG, "findBackupFileId: failed", e)
            if (e.message?.contains("quotaExceeded", ignoreCase = true) == true) {
                throw GoogleDriveQuotaException("Storage quota exceeded")
            }
            null
        }
    }

    private fun createBackupFile(drive: Drive, csvBytes: ByteArray) {
        try {
            Log.d(TAG, "createBackupFile: uploading new file")
            val fileMetadata = DriveFile().apply {
                name = BACKUP_FILE_NAME
                setParents(arrayListOf("appDataFolder"))
            }
            val tempFile = File.createTempFile("backup", ".csv", context.cacheDir)
            tempFile.writeBytes(csvBytes)
            val mediaContent = FileContent("text/csv", tempFile)

            drive.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute()

            Log.d(TAG, "createBackupFile: upload successful")
            tempFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "createBackupFile: failed", e)
            if (e.message?.contains("quotaExceeded", ignoreCase = true) == true ||
                e.message?.contains("insufficientStorage", ignoreCase = true) == true) {
                throw GoogleDriveQuotaException("Storage quota exceeded")
            }
            throw IOException("Failed to create backup file", e)
        }
    }

    private fun updateBackupFile(drive: Drive, fileId: String, csvBytes: ByteArray) {
        try {
            Log.d(TAG, "updateBackupFile: updating existing file $fileId")
            val tempFile = File.createTempFile("backup", ".csv", context.cacheDir)
            tempFile.writeBytes(csvBytes)
            val mediaContent = FileContent("text/csv", tempFile)

            drive.files().update(fileId, null, mediaContent)
                .execute()

            Log.d(TAG, "updateBackupFile: update successful")
            tempFile.delete()
        } catch (e: Exception) {
            Log.e(TAG, "updateBackupFile: failed", e)
            if (e.message?.contains("quotaExceeded", ignoreCase = true) == true ||
                e.message?.contains("insufficientStorage", ignoreCase = true) == true) {
                throw GoogleDriveQuotaException("Storage quota exceeded")
            }
            throw IOException("Failed to update backup file", e)
        }
    }

    private fun downloadBackup(drive: Drive, fileId: String): ByteArray {
        return try {
            Log.d(TAG, "downloadBackup: downloading file $fileId")
            val outputStream = ByteArrayOutputStream()
            drive.files().get(fileId)
                .executeMediaAndDownloadTo(outputStream)
            Log.d(TAG, "downloadBackup: download successful")
            outputStream.toByteArray()
        } catch (e: Exception) {
            Log.e(TAG, "downloadBackup: failed", e)
            throw IOException("Failed to download backup", e)
        }
    }

    private fun authorizationRequest(
        account: Account? = null,
        promptSelectAccount: Boolean = false
    ): AuthorizationRequest {
        val builder = AuthorizationRequest.builder()
            .setRequestedScopes(requestedScopes())

        if (account != null) {
            builder.setAccount(account)
        }
        if (promptSelectAccount) {
            builder.setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT)
        }
        return builder.build()
    }

    private fun requestedScopes(): List<Scope> {
        return listOf(
            Scope(DRIVE_APPDATA_SCOPE),
            Scope(USERINFO_EMAIL_SCOPE)
        )
    }

    companion object {
        const val BACKUP_FILE_NAME = "presently_backup.csv"
        private const val GOOGLE_ACCOUNT_TYPE = "com.google"
        private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
        private const val USERINFO_EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email"
        private const val TAG = "GoogleDriveBackup"
    }
}

data class GoogleDriveBackupFile(
    val id: String,
    val modifiedTime: String,
    val entries: List<Entry>
)

private class GoogleDriveAuthException(message: String) : IOException(message)
private class GoogleDriveQuotaException(message: String) : IOException(message)
