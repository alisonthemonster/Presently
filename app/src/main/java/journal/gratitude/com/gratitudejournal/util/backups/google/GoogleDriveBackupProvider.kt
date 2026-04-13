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
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URLEncoder
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
                val accessToken = getAccessToken(accountEmail)
                Log.d(TAG, "uploadToCloud: access token obtained")
                val existingFile = findBackupFile(accessToken)
                val csvBytes = file.readBytes()
                Log.d(TAG, "uploadToCloud: existing file=${existingFile?.id}, csvBytes=${csvBytes.size}")
                val responseCode = if (existingFile == null) {
                    uploadNewFile(accessToken, csvBytes)
                } else {
                    updateExistingFile(accessToken, existingFile.id, csvBytes)
                }

                Log.d(TAG, "uploadToCloud: response code=$responseCode")
                when {
                    responseCode in 200..299 -> BackupUploadSuccess
                    responseCode == HttpURLConnection.HTTP_UNAUTHORIZED -> BackupAuthFailure(
                        IOException("Google Drive token rejected with HTTP $responseCode")
                    )
                    else -> BackupUploadFailure(
                        IOException("Google Drive upload failed with HTTP $responseCode")
                    )
                }
            } catch (exception: GoogleDriveAuthResolutionRequiredException) {
                Log.e(TAG, "uploadToCloud: auth resolution required (token expired?)", exception)
                BackupAuthFailure(exception)
            } catch (exception: GoogleDriveAuthException) {
                Log.e(TAG, "uploadToCloud: auth exception", exception)
                BackupAuthFailure(exception)
            } catch (exception: GoogleDriveQuotaException) {
                Log.e(TAG, "uploadToCloud: storage quota exceeded", exception)
                BackupStorageFullFailure(exception)
            } catch (exception: IOException) {
                Log.e(TAG, "uploadToCloud: IO exception", exception)
                BackupUploadFailure(exception)
            }
        }
    }

    suspend fun getBackupForRestore(accountEmail: String): GoogleDriveBackupFile? {
        return withContext(Dispatchers.IO) {
            val accessToken = getAccessToken(accountEmail)
            val backupMetadata = findBackupFile(accessToken) ?: return@withContext null
            val contents = downloadBackup(accessToken, backupMetadata.id)
            val parser = CSVParser.parse(
                ByteArrayInputStream(contents),
                StandardCharsets.UTF_8,
                CSVFormat.DEFAULT
            )
            val entries = convertCsvToEntries(RealCsvParser(parser))
            GoogleDriveBackupFile(
                id = backupMetadata.id,
                modifiedTime = backupMetadata.modifiedTime,
                entries = entries
            )
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
        Log.d(TAG, "resolveAuthorizedAccountEmail: fetching user email with access token")
        return withContext(Dispatchers.IO) {
            fetchUserEmail(accessToken)
        }
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

    private suspend fun getAccessToken(accountEmail: String): String {
        Log.d(TAG, "getAccessToken: requesting token for $accountEmail")
        val account = Account(accountEmail, GOOGLE_ACCOUNT_TYPE)
        val result = authorizationClient
            .authorize(authorizationRequest(account = account))
            .await()

        if (result.hasResolution()) {
            Log.w(TAG, "getAccessToken: result requires resolution (consent needed), cannot proceed silently")
            throw GoogleDriveAuthResolutionRequiredException(
                "Google Drive token refresh requires user interaction"
            )
        }

        return result.accessToken
            ?: run {
                Log.e(TAG, "getAccessToken: no access token in result (grantedScopes=${result.grantedScopes})")
                throw GoogleDriveAuthException("Google Drive authorization did not return an access token")
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

    private fun fetchUserEmail(accessToken: String): String? {
        Log.d(TAG, "fetchUserEmail: calling userinfo endpoint")
        val connection = openConnection(
            "https://www.googleapis.com/oauth2/v2/userinfo",
            accessToken,
            "GET"
        )
        return connection.useAndReadResponse { body ->
            Log.d(TAG, "fetchUserEmail: response code=$responseCode, body length=${body.length}")
            if (responseCode !in 200..299) {
                throw mapHttpException(responseCode, body)
            }
            val email = JSONObject(body).optString("email").takeIf { it.isNotBlank() }
            Log.d(TAG, "fetchUserEmail: email=${if (email != null) "present" else "null/blank"}")
            email
        }
    }

    private fun findBackupFile(accessToken: String): GoogleDriveFileMetadata? {
        val query = URLEncoder.encode(
            "name='$BACKUP_FILE_NAME' and trashed=false",
            StandardCharsets.UTF_8.name()
        )
        val url =
            "https://www.googleapis.com/drive/v3/files?spaces=appDataFolder&pageSize=1&fields=files(id,name,modifiedTime)&q=$query"
        val connection = openConnection(url, accessToken, "GET")
        return connection.useAndReadResponse { body ->
            if (responseCode !in 200..299) {
                throw mapHttpException(responseCode, body)
            }
            val files = JSONObject(body).optJSONArray("files") ?: JSONArray()
            if (files.length() == 0) {
                null
            } else {
                val file = files.getJSONObject(0)
                GoogleDriveFileMetadata(
                    id = file.getString("id"),
                    modifiedTime = file.optString("modifiedTime")
                )
            }
        }
    }

    private fun downloadBackup(accessToken: String, fileId: String): ByteArray {
        val connection = openConnection(
            "https://www.googleapis.com/drive/v3/files/$fileId?alt=media",
            accessToken,
            "GET"
        )
        return connection.useAndReadBytes { bytes, body ->
            if (responseCode !in 200..299) {
                throw mapHttpException(responseCode, body)
            }
            bytes
        }
    }

    private fun uploadNewFile(accessToken: String, csvBytes: ByteArray): Int {
        val connection = openConnection(
            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart",
            accessToken,
            "POST"
        )
        return executeMultipartUpload(
            connection = connection,
            metadataJson = JSONObject()
                .put("name", BACKUP_FILE_NAME)
                .put("parents", JSONArray().put("appDataFolder"))
                .toString(),
            csvBytes = csvBytes
        )
    }

    private fun updateExistingFile(accessToken: String, fileId: String, csvBytes: ByteArray): Int {
        val connection = openConnection(
            "https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=multipart",
            accessToken,
            "PATCH"
        )
        return executeMultipartUpload(
            connection = connection,
            metadataJson = JSONObject().put("name", BACKUP_FILE_NAME).toString(),
            csvBytes = csvBytes
        )
    }

    private fun executeMultipartUpload(
        connection: HttpURLConnection,
        metadataJson: String,
        csvBytes: ByteArray
    ): Int {
        val boundary = "presently-backup-boundary"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "multipart/related; boundary=$boundary")

        val payload = ByteArrayOutputStream().apply {
            write("--$boundary\r\n".toByteArray())
            write("Content-Type: application/json; charset=UTF-8\r\n\r\n".toByteArray())
            write(metadataJson.toByteArray(StandardCharsets.UTF_8))
            write("\r\n--$boundary\r\n".toByteArray())
            write("Content-Type: text/csv\r\n\r\n".toByteArray())
            write(csvBytes)
            write("\r\n--$boundary--".toByteArray())
        }.toByteArray()

        connection.outputStream.use { outputStream ->
            outputStream.write(payload)
        }

        val body = connection.readResponseBody()
        if (connection.responseCode !in 200..299) {
            throw mapHttpException(connection.responseCode, body)
        }
        return connection.responseCode
    }

    private fun openConnection(
        url: String,
        accessToken: String,
        method: String
    ): HttpURLConnection {
        return (java.net.URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            setRequestProperty("Authorization", "Bearer $accessToken")
            setRequestProperty("Accept", "application/json")
            connectTimeout = 15_000
            readTimeout = 15_000
        }
    }

    private fun HttpURLConnection.readResponseBody(): String {
        val stream = if (responseCode in 200..299) inputStream else errorStream
        return stream?.bufferedReader()?.use { it.readText() }.orEmpty()
    }

    private fun mapHttpException(responseCode: Int, responseBody: String): IOException {
        if (
            responseCode == HttpURLConnection.HTTP_UNAUTHORIZED ||
            responseCode == HttpURLConnection.HTTP_FORBIDDEN ||
            responseBody.contains("invalidCredentials", ignoreCase = true) ||
            responseBody.contains("insufficientPermissions", ignoreCase = true)
        ) {
            return GoogleDriveAuthException("Google Drive authorization failed")
        }
        if (
            responseBody.contains("storageQuotaExceeded", ignoreCase = true) ||
            responseBody.contains("insufficientStorage", ignoreCase = true)
        ) {
            return GoogleDriveQuotaException(responseBody)
        }
        return IOException("Google Drive request failed with HTTP $responseCode")
    }

    private inline fun <T> HttpURLConnection.useAndReadResponse(block: HttpURLConnection.(String) -> T): T {
        return try {
            block(readResponseBody())
        } finally {
            disconnect()
        }
    }

    private inline fun <T> HttpURLConnection.useAndReadBytes(
        block: HttpURLConnection.(ByteArray, String) -> T
    ): T {
        val bytes = if (responseCode in 200..299) {
            BufferedInputStream(inputStream).use { it.readBytes() }
        } else {
            ByteArray(0)
        }
        val body = if (responseCode in 200..299) {
            ""
        } else {
            errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
        }
        return try {
            block(bytes, body)
        } finally {
            disconnect()
        }
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

private data class GoogleDriveFileMetadata(
    val id: String,
    val modifiedTime: String
)

private class GoogleDriveAuthException(message: String) : IOException(message)
private class GoogleDriveAuthResolutionRequiredException(message: String) : IOException(message)

private class GoogleDriveQuotaException(message: String) : IOException(message)
