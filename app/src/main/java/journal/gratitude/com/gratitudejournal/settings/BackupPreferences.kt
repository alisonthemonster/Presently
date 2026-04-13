package journal.gratitude.com.gratitudejournal.settings

import android.content.SharedPreferences
import android.text.format.DateUtils
import com.dropbox.core.oauth.DbxCredential
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.DROPBOX_AUTH_QUIT
import journal.gratitude.com.gratitudejournal.logging.DROPBOX_AUTH_SUCCESS
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupPreferences @Inject constructor(
    private val sharedPrefs: SharedPreferences,
    private val analytics: AnalyticsLogger
) {

    fun getDropboxState(): BackupProviderState {
        return BackupProviderState(
            isConnected = getDropboxCredential() != null,
            accountEmail = sharedPrefs.getString(DROPBOX_ACCOUNT_EMAIL, null),
            lastBackupTimestamp = sharedPrefs.getLong(DROPBOX_LAST_BACKUP_TIMESTAMP, NO_TIMESTAMP)
                .takeIf { it != NO_TIMESTAMP },
            frequency = BackupFrequency.fromStorageValue(
                sharedPrefs.getString(DROPBOX_BACKUP_FREQUENCY, null)
            )
        )
    }

    fun getGoogleDriveState(): BackupProviderState {
        return BackupProviderState(
            isConnected = sharedPrefs.getBoolean(GOOGLE_DRIVE_CONNECTED, false),
            accountEmail = sharedPrefs.getString(GOOGLE_DRIVE_ACCOUNT_EMAIL, null),
            lastBackupTimestamp = sharedPrefs.getLong(GOOGLE_DRIVE_LAST_BACKUP_TIMESTAMP, NO_TIMESTAMP)
                .takeIf { it != NO_TIMESTAMP },
            frequency = BackupFrequency.fromStorageValue(
                sharedPrefs.getString(GOOGLE_DRIVE_BACKUP_FREQUENCY, null)
            )
        )
    }

    fun getState(provider: BackupProvider): BackupProviderState {
        return when (provider) {
            BackupProvider.DROPBOX -> getDropboxState()
            BackupProvider.GOOGLE_DRIVE -> getGoogleDriveState()
        }
    }

    fun updateFrequency(provider: BackupProvider, frequency: BackupFrequency) {
        sharedPrefs.edit()
            .putString(provider.frequencyKey, frequency.storageValue)
            .apply()
    }

    fun updateLastBackupTimestamp(provider: BackupProvider, timestamp: Long) {
        sharedPrefs.edit()
            .putLong(provider.lastBackupTimestampKey, timestamp)
            .apply()
    }

    fun clearLastBackupTimestamp(provider: BackupProvider) {
        sharedPrefs.edit()
            .remove(provider.lastBackupTimestampKey)
            .apply()
    }

    fun getDropboxCredential(): DbxCredential? {
        val serializedToken = sharedPrefs.getString(DROPBOX_ACCESS_TOKEN, null)
        return when {
            serializedToken == DROPBOX_AUTH_ATTEMPTED_VALUE -> null
            serializedToken == null -> null
            serializedToken.contains("{") -> DbxCredential.Reader.readFully(serializedToken)
            else -> DbxCredential(serializedToken)
        }
    }

    fun setDropboxCredential(newToken: DbxCredential, accountEmail: String?) {
        analytics.recordEvent(DROPBOX_AUTH_SUCCESS)
        sharedPrefs.edit()
            .putString(DROPBOX_ACCESS_TOKEN, newToken.toString())
            .putBoolean(DROPBOX_CONNECTED, true)
            .putString(DROPBOX_ACCOUNT_EMAIL, accountEmail)
            .apply()
    }

    fun markDropboxAuthInitiated() {
        sharedPrefs.edit()
            .putString(DROPBOX_ACCESS_TOKEN, DROPBOX_AUTH_ATTEMPTED_VALUE)
            .apply()
    }

    fun wasDropboxAuthInitiated(): Boolean {
        val token = sharedPrefs.getString(DROPBOX_ACCESS_TOKEN, null) ?: return false
        return token == DROPBOX_AUTH_ATTEMPTED_VALUE
    }

    fun markDropboxAuthCancelled() {
        analytics.recordEvent(DROPBOX_AUTH_QUIT)
        sharedPrefs.edit()
            .putBoolean(DROPBOX_CONNECTED, false)
            .remove(DROPBOX_ACCESS_TOKEN)
            .remove(DROPBOX_ACCOUNT_EMAIL)
            .apply()
    }

    fun clearDropboxConnection() {
        sharedPrefs.edit()
            .putBoolean(DROPBOX_CONNECTED, false)
            .remove(DROPBOX_ACCESS_TOKEN)
            .remove(DROPBOX_ACCOUNT_EMAIL)
            .remove(DROPBOX_LAST_BACKUP_TIMESTAMP)
            .apply()
    }

    fun setDropboxAccountEmail(accountEmail: String?) {
        sharedPrefs.edit()
            .putString(DROPBOX_ACCOUNT_EMAIL, accountEmail)
            .apply()
    }

    fun setGoogleDriveConnection(accountEmail: String) {
        sharedPrefs.edit()
            .putBoolean(GOOGLE_DRIVE_CONNECTED, true)
            .putString(GOOGLE_DRIVE_ACCOUNT_EMAIL, accountEmail)
            .apply()
    }

    fun clearGoogleDriveConnection() {
        sharedPrefs.edit()
            .putBoolean(GOOGLE_DRIVE_CONNECTED, false)
            .remove(GOOGLE_DRIVE_ACCOUNT_EMAIL)
            .remove(GOOGLE_DRIVE_LAST_BACKUP_TIMESTAMP)
            .apply()
    }

    companion object {
        private const val DROPBOX_CONNECTED = "dropbox_pref"
        private const val DROPBOX_ACCESS_TOKEN = "access-token"
        private const val DROPBOX_ACCOUNT_EMAIL = "dropbox_account_email"
        private const val DROPBOX_BACKUP_FREQUENCY = "dropbox_backup_frequency"
        private const val DROPBOX_LAST_BACKUP_TIMESTAMP = "dropbox_last_backup_timestamp"
        private const val GOOGLE_DRIVE_CONNECTED = "google_drive_connected"
        private const val GOOGLE_DRIVE_ACCOUNT_EMAIL = "google_drive_account_email"
        private const val GOOGLE_DRIVE_BACKUP_FREQUENCY = "google_drive_backup_frequency"
        private const val GOOGLE_DRIVE_LAST_BACKUP_TIMESTAMP = "google_drive_last_backup_timestamp"
        private const val DROPBOX_AUTH_ATTEMPTED_VALUE = "attempted"
        private const val NO_TIMESTAMP = -1L
    }
}

data class BackupProviderState(
    val isConnected: Boolean,
    val accountEmail: String?,
    val lastBackupTimestamp: Long?,
    val frequency: BackupFrequency
)

enum class BackupFrequency(
    val storageValue: String,
    val intervalMillis: Long?
) {
    DAILY("daily", DateUtils.DAY_IN_MILLIS),
    MONTHLY("monthly", 30L * DateUtils.DAY_IN_MILLIS),
    ON_EVERY_CHANGE("on_every_change", null);

    companion object {
        fun fromStorageValue(value: String?): BackupFrequency {
            return when (value) {
                "monthly", "1" -> MONTHLY
                "on_every_change", "2" -> ON_EVERY_CHANGE
                else -> DAILY
            }
        }
    }
}

enum class BackupProvider(
    val notificationId: Int,
    val workerTag: String,
    val uniqueWorkName: String,
    internal val frequencyKey: String,
    internal val lastBackupTimestampKey: String
) {
    DROPBOX(
        notificationId = 98104,
        workerTag = "DROPBOX_PRESENTLY_BACKUP",
        uniqueWorkName = "dropbox_presently_backup",
        frequencyKey = "dropbox_backup_frequency",
        lastBackupTimestampKey = "dropbox_last_backup_timestamp"
    ),
    GOOGLE_DRIVE(
        notificationId = 98105,
        workerTag = "GOOGLE_DRIVE_PRESENTLY_BACKUP",
        uniqueWorkName = "google_drive_presently_backup",
        frequencyKey = "google_drive_backup_frequency",
        lastBackupTimestampKey = "google_drive_last_backup_timestamp"
    )
}
