package journal.gratitude.com.gratitudejournal.util.backups.status

import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import java.util.concurrent.TimeUnit

enum class UploadFailureType {
    AUTH,
    STORAGE_FULL,

    /** Offline, DNS, timeouts. Usually self-heals, so it surfaces through the overdue check. */
    NETWORK,

    /** Rate limiting or Dropbox 5xx. Usually self-heals, so it surfaces through the overdue check. */
    TRANSIENT_SERVER,
    OTHER;

    val isTransient: Boolean get() = this == NETWORK || this == TRANSIENT_SERVER
}

data class UploadFailure(val type: UploadFailureType, val atMillis: Long)

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
        val lastExportAtMillis: Long,
        val entriesSinceExport: Int
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
    val entriesSinceLastManualExport: Int
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
        val lastExport = input.lastManualExportAtMillis

        if (lastExport == null) {
            return if (input.totalEntries >= NEVER_BACKED_UP_MIN_ENTRIES) {
                BackupStatus.NeverBackedUp(input.totalEntries)
            } else {
                BackupStatus.Healthy
            }
        }

        val isOld = input.nowMillis - lastExport > STALE_AFTER_MILLIS
        return if (isOld && input.entriesSinceLastManualExport > 0) {
            BackupStatus.Stale(lastExport, input.entriesSinceLastManualExport)
        } else {
            BackupStatus.Healthy
        }
    }

    private fun isOverdue(input: BackupStatusInput): Boolean {
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
        return nowMillis - dismissal.atMillis >= snooze
    }
}
