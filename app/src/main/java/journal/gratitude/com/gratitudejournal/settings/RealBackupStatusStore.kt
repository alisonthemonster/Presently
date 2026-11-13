package journal.gratitude.com.gratitudejournal.settings

import android.content.SharedPreferences
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupBannerDismissal
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusType
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailureType

/**
 * Mutating methods are synchronized because the periodic and "every change" upload workers can
 * finish at the same time, and recording a failure is a read-merge-write.
 */
class RealBackupStatusStore(
    private val sharedPrefs: SharedPreferences,
    private val now: () -> Long = System::currentTimeMillis
) : BackupStatusStore {

    override fun getLastDropboxSuccessAt(): Long? = sharedPrefs.getLongOrNull(LAST_DROPBOX_SUCCESS_AT)

    override fun getLastManualExportAt(): Long? = sharedPrefs.getLongOrNull(LAST_MANUAL_EXPORT_AT)

    override fun getLastUploadFailure(): UploadFailure? {
        val type = UploadFailureType.fromCode(sharedPrefs.getString(LAST_FAILURE_TYPE, null))
            ?: return null
        val at = sharedPrefs.getLongOrNull(LAST_FAILURE_AT) ?: return null
        return UploadFailure(type, at)
    }

    override fun getDropboxEnabledAt(): Long? = sharedPrefs.getLongOrNull(DROPBOX_ENABLED_AT)

    override fun wasDropboxDisconnectedUnexpectedly(): Boolean =
        sharedPrefs.getBoolean(DROPBOX_DISCONNECTED_UNEXPECTEDLY, false)

    @Synchronized
    override fun recordDropboxUploadSuccess() {
        sharedPrefs.edit()
            .putLong(LAST_DROPBOX_SUCCESS_AT, now())
            .remove(LAST_FAILURE_TYPE)
            .remove(LAST_FAILURE_AT)
            .apply()
    }

    @Synchronized
    override fun recordDropboxUploadFailure(type: UploadFailureType) {
        val merged = UploadFailure.merge(getLastUploadFailure(), UploadFailure(type, now()))
        sharedPrefs.edit()
            .putString(LAST_FAILURE_TYPE, merged.type.code)
            .putLong(LAST_FAILURE_AT, merged.atMillis)
            .apply()
    }

    @Synchronized
    override fun recordManualExport() {
        sharedPrefs.edit().putLong(LAST_MANUAL_EXPORT_AT, now()).apply()
    }

    @Synchronized
    override fun seedDropboxBaselineIfMissing() {
        if (!sharedPrefs.contains(DROPBOX_ENABLED_AT)) {
            sharedPrefs.edit().putLong(DROPBOX_ENABLED_AT, now()).apply()
        }
    }

    @Synchronized
    override fun recordDropboxConnected() {
        sharedPrefs.edit()
            .putLong(DROPBOX_ENABLED_AT, now())
            .remove(DROPBOX_DISCONNECTED_UNEXPECTEDLY)
            .remove(LAST_FAILURE_TYPE)
            .remove(LAST_FAILURE_AT)
            .apply()
    }

    @Synchronized
    override fun recordDropboxDisconnectedByUser() {
        sharedPrefs.edit()
            .remove(DROPBOX_ENABLED_AT)
            .remove(DROPBOX_DISCONNECTED_UNEXPECTEDLY)
            .remove(LAST_FAILURE_TYPE)
            .remove(LAST_FAILURE_AT)
            .apply()
    }

    @Synchronized
    override fun markDropboxDisconnectedUnexpectedly() {
        sharedPrefs.edit().putBoolean(DROPBOX_DISCONNECTED_UNEXPECTEDLY, true).apply()
    }

    override fun getBannerDismissal(): BackupBannerDismissal? {
        val type = sharedPrefs.getString(BANNER_DISMISSED_TYPE, null)
            ?.let { name -> BackupStatusType.entries.firstOrNull { it.name == name } }
            ?: return null
        val at = sharedPrefs.getLongOrNull(BANNER_DISMISSED_AT) ?: return null
        return BackupBannerDismissal(type, at)
    }

    @Synchronized
    override fun recordBannerDismissal(type: BackupStatusType) {
        sharedPrefs.edit()
            .putString(BANNER_DISMISSED_TYPE, type.name)
            .putLong(BANNER_DISMISSED_AT, now())
            .apply()
    }

    private fun SharedPreferences.getLongOrNull(key: String): Long? =
        if (contains(key)) getLong(key, 0L) else null

    companion object {
        const val LAST_DROPBOX_SUCCESS_AT = "backup_last_dropbox_success_at"
        const val LAST_MANUAL_EXPORT_AT = "backup_last_manual_export_at"
        const val LAST_FAILURE_TYPE = "backup_last_failure_type"
        const val LAST_FAILURE_AT = "backup_last_failure_at"
        const val DROPBOX_ENABLED_AT = "backup_dropbox_enabled_at"
        const val BANNER_DISMISSED_TYPE = "backup_banner_dismissed_type"
        const val BANNER_DISMISSED_AT = "backup_banner_dismissed_at"
        const val DROPBOX_DISCONNECTED_UNEXPECTEDLY = "backup_dropbox_disconnected_unexpectedly"
    }
}
