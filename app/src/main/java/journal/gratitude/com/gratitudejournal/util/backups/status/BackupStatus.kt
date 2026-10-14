package journal.gratitude.com.gratitudejournal.util.backups.status

import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import java.util.concurrent.TimeUnit

/**
 * [code] is what gets persisted, so it must never change for an existing value.
 */
enum class UploadFailureType(val code: String) {
    AUTH("auth"),
    STORAGE_FULL("storage_full"),

    /** Offline, DNS, timeouts. Usually self-heals, so it surfaces through the overdue check. */
    NETWORK("network"),

    /** Rate limiting or Dropbox 5xx. Usually self-heals, so it surfaces through the overdue check. */
    TRANSIENT_SERVER("transient_server"),
    OTHER("other");

    val isTransient: Boolean get() = this == NETWORK || this == TRANSIENT_SERVER

    companion object {
        fun fromCode(code: String?): UploadFailureType? = entries.firstOrNull { it.code == code }
    }
}

data class UploadFailure(val type: UploadFailureType, val atMillis: Long) {
    companion object {
        /**
         * Decides which failure to persist after a new one. A transient failure carries no new
         * information about an unresolved actionable one (full, auth, other), so it must not
         * overwrite it; only a success clears it.
         */
        fun merge(existing: UploadFailure?, incoming: UploadFailure): UploadFailure =
            if (existing != null && !existing.type.isTransient && incoming.type.isTransient) {
                existing
            } else {
                incoming
            }
    }
}

enum class BackupSource { MANUAL, DROPBOX }

enum class BackupSeverity { NONE, LOW, MEDIUM, HIGH }

enum class BackupStatusType(val severity: BackupSeverity) {
    HEALTHY(BackupSeverity.NONE),
    DROPBOX_DISCONNECTED(BackupSeverity.HIGH),
    DROPBOX_FULL(BackupSeverity.HIGH),
    DROPBOX_FAILING(BackupSeverity.HIGH),
    NEVER_BACKED_UP(BackupSeverity.MEDIUM),
    STALE(BackupSeverity.LOW)
}

sealed interface BackupStatus {
    val type: BackupStatusType

    data object Healthy : BackupStatus {
        override val type = BackupStatusType.HEALTHY
    }

    data object DropboxDisconnected : BackupStatus {
        override val type = BackupStatusType.DROPBOX_DISCONNECTED
    }

    data object DropboxFull : BackupStatus {
        override val type = BackupStatusType.DROPBOX_FULL
    }

    /**
     * @param lastAttemptFailed the most recent upload attempt failed
     * @param lastSuccessAtMillis last successful Dropbox upload, used for "hasn't backed up since" copy
     */
    data class DropboxFailing(
        val lastAttemptFailed: Boolean,
        val lastSuccessAtMillis: Long?
    ) : BackupStatus {
        override val type = BackupStatusType.DROPBOX_FAILING
    }

    data class NeverBackedUp(val entryCount: Int) : BackupStatus {
        override val type = BackupStatusType.NEVER_BACKED_UP
    }

    data class Stale(
        val lastBackupAtMillis: Long,
        val lastBackupSource: BackupSource,
        val entriesSinceBackup: Int
    ) : BackupStatus {
        override val type = BackupStatusType.STALE
    }
}

data class BackupStatusInput(
    val nowMillis: Long,
    val dropboxConnected: Boolean,
    val dropboxDisconnectedUnexpectedly: Boolean,
    val cadence: BackupCadence,
    val dropboxEnabledAtMillis: Long?,
    val lastDropboxSuccessAtMillis: Long?,
    val lastUploadFailure: UploadFailure?,
    val lastManualExportAtMillis: Long?,
    val totalEntries: Int,
    /** Entries added or changed since the most recent backup of either kind. */
    val entriesSinceLastBackup: Int
)

data class BackupBannerDismissal(val type: BackupStatusType, val atMillis: Long)

object BackupStatusResolver {

    const val NEVER_BACKED_UP_MIN_ENTRIES = 10

    private val GRACE_MILLIS = TimeUnit.HOURS.toMillis(12)
    private val DAILY_OVERDUE_MILLIS = TimeUnit.DAYS.toMillis(1) + GRACE_MILLIS
    private val WEEKLY_OVERDUE_MILLIS = TimeUnit.DAYS.toMillis(7) + GRACE_MILLIS
    private val STALE_AFTER_MILLIS = TimeUnit.DAYS.toMillis(30)

    private val MEDIUM_SNOOZE_MILLIS = TimeUnit.DAYS.toMillis(14)
    private val LOW_SNOOZE_MILLIS = TimeUnit.DAYS.toMillis(30)

    fun resolve(input: BackupStatusInput): BackupStatus {
        if (input.dropboxDisconnectedUnexpectedly) {
            return BackupStatus.DropboxDisconnected
        }

        if (input.dropboxConnected) {
            return resolveConnected(input)
        }

        return resolveNotConnected(input)
    }

    private fun resolveConnected(input: BackupStatusInput): BackupStatus {
        // A failure only counts while no success has happened since.
        val activeFailure = input.lastUploadFailure?.takeIf { failure ->
            val success = input.lastDropboxSuccessAtMillis
            success == null || failure.atMillis > success
        }

        if (activeFailure?.type == UploadFailureType.STORAGE_FULL) {
            return BackupStatus.DropboxFull
        }

        // Transient failures don't flag immediately; a sustained problem is caught by isOverdue.
        if (activeFailure != null && !activeFailure.type.isTransient) {
            return BackupStatus.DropboxFailing(
                lastAttemptFailed = true,
                lastSuccessAtMillis = input.lastDropboxSuccessAtMillis
            )
        }

        if (isOverdue(input)) {
            return BackupStatus.DropboxFailing(
                lastAttemptFailed = false,
                lastSuccessAtMillis = input.lastDropboxSuccessAtMillis
            )
        }

        // Manual exports intentionally never affect the health of a Dropbox user.
        return BackupStatus.Healthy
    }

    private fun resolveNotConnected(input: BackupStatusInput): BackupStatus {
        // Past Dropbox backups still count once Dropbox has been turned off.
        val manual = input.lastManualExportAtMillis
        val dropbox = input.lastDropboxSuccessAtMillis
        val (lastBackup, source) = when {
            manual != null && (dropbox == null || manual >= dropbox) -> manual to BackupSource.MANUAL
            dropbox != null -> dropbox to BackupSource.DROPBOX
            else -> null to null
        }

        if (lastBackup == null || source == null) {
            return if (input.totalEntries >= NEVER_BACKED_UP_MIN_ENTRIES) {
                BackupStatus.NeverBackedUp(input.totalEntries)
            } else {
                BackupStatus.Healthy
            }
        }

        val isOld = input.nowMillis - lastBackup > STALE_AFTER_MILLIS
        return if (isOld && input.entriesSinceLastBackup > 0) {
            BackupStatus.Stale(lastBackup, source, input.entriesSinceLastBackup)
        } else {
            BackupStatus.Healthy
        }
    }

    private fun isOverdue(input: BackupStatusInput): Boolean {
        // The uploader no-ops (and records no success) for an empty journal, so there's nothing to be overdue on.
        if (input.totalEntries == 0) return false

        val threshold = when (input.cadence) {
            BackupCadence.DAILY -> DAILY_OVERDUE_MILLIS
            BackupCadence.WEEKLY -> WEEKLY_OVERDUE_MILLIS
            // No schedule to be overdue against; only a failed attempt counts.
            BackupCadence.EVERY_CHANGE -> return false
        }
        // Measure from whichever is later so a fresh connection isn't instantly "overdue".
        val baseline = listOfNotNull(
            input.lastDropboxSuccessAtMillis,
            input.dropboxEnabledAtMillis
        ).maxOrNull() ?: return false

        return input.nowMillis - baseline > threshold
    }

    /**
     * High severity states cannot be dismissed. Medium/Low are hidden while snoozed, and any
     * change of state type re-shows the banner.
     */
    fun shouldShowBanner(
        status: BackupStatus,
        dismissal: BackupBannerDismissal?,
        nowMillis: Long
    ): Boolean {
        val snooze = when (status.type.severity) {
            BackupSeverity.NONE -> return false
            BackupSeverity.HIGH -> return true
            BackupSeverity.MEDIUM -> MEDIUM_SNOOZE_MILLIS
            BackupSeverity.LOW -> LOW_SNOOZE_MILLIS
        }
        if (dismissal == null || dismissal.type != status.type) return true
        val elapsed = nowMillis - dismissal.atMillis
        // A dismissal in the future (clock moved backwards) is treated as expired.
        return elapsed < 0 || elapsed >= snooze
    }
}
