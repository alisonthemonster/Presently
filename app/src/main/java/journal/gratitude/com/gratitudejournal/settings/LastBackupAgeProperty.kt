package journal.gratitude.com.gratitudejournal.settings

import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import java.util.concurrent.TimeUnit

/**
 * How long ago the user last backed up, as a coarse bucket: `today`, `1_7_days`, `8_30_days`,
 * `over_30_days`, or `unknown` when nothing has been recorded. `unknown` means "no record", not
 * "never": backups made before this was tracked aren't known.
 *
 * Follows the same rule as the backup status logic: a user with Dropbox connected is judged on
 * Dropbox alone, so a manual export never hides a sync that has stopped. Everyone else is
 * judged on their latest backup of either kind.
 */
internal const val LAST_BACKUP_AGE_PROPERTY = "last_backup_age"

/** The backup time the age is measured from, see [LAST_BACKUP_AGE_PROPERTY]. */
internal fun lastBackupAtMillis(
    dropboxConnected: Boolean,
    lastDropboxSuccessAtMillis: Long?,
    lastManualExportAtMillis: Long?
): Long? =
    if (dropboxConnected) {
        lastDropboxSuccessAtMillis
    } else {
        listOfNotNull(lastDropboxSuccessAtMillis, lastManualExportAtMillis).maxOrNull()
    }

/** Buckets by whole days elapsed, so `today` is the last 24 hours rather than the calendar day. */
internal fun lastBackupAgeValue(lastBackupAtMillis: Long?, nowMillis: Long): String {
    if (lastBackupAtMillis == null) return "unknown"
    val days = TimeUnit.MILLISECONDS.toDays(nowMillis - lastBackupAtMillis)
    return when {
        days < 1 -> "today" // includes a backup time in the future, e.g. after the clock moved back
        days <= 7 -> "1_7_days"
        days <= 30 -> "8_30_days"
        else -> "over_30_days"
    }
}

/** Call at launch. */
internal fun AnalyticsLogger.updateLastBackupAgeProperty(
    settings: PresentlySettings,
    store: BackupStatusStore,
    nowMillis: Long = System.currentTimeMillis()
) {
    val lastBackupAt = lastBackupAtMillis(
        dropboxConnected = settings.getAccessToken() != null,
        lastDropboxSuccessAtMillis = store.getLastDropboxSuccessAt(),
        lastManualExportAtMillis = store.getLastManualExportAt()
    )
    setUserProperty(LAST_BACKUP_AGE_PROPERTY, lastBackupAgeValue(lastBackupAt, nowMillis))
}
