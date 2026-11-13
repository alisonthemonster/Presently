package journal.gratitude.com.gratitudejournal.ui.timeline

import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.settings.BackupStatusStore
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupSeverity
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatus
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusInput
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusResolver
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

internal const val BACKUP_CARD_ID = "backup_status"

/** Gathers what [BackupStatusResolver] needs from the store, settings and the journal. */
internal fun buildBackupStatusInput(
    settings: PresentlySettings,
    store: BackupStatusStore,
    entries: List<Entry>,
    nowMillis: Long,
    zone: ZoneId = ZoneId.systemDefault()
): BackupStatusInput {
    val written = entries.filter { it.entryContent.isNotEmpty() }
    val lastDropbox = store.getLastDropboxSuccessAt()
    val lastManual = store.getLastManualExportAt()
    val lastBackup = listOfNotNull(lastDropbox, lastManual).maxOrNull()
    // Entries have no edit time, so "since the backup" is entries dated after the backup's day.
    val sinceLastBackup = if (lastBackup == null) {
        written.size
    } else {
        val backupDate = Instant.ofEpochMilli(lastBackup).atZone(zone).toLocalDate()
        written.count { it.entryDate.isAfter(backupDate) }
    }
    return BackupStatusInput(
        nowMillis = nowMillis,
        dropboxConnected = settings.getAccessToken() != null,
        dropboxDisconnectedUnexpectedly = store.wasDropboxDisconnectedUnexpectedly(),
        cadence = settings.getAutomaticBackupCadence(),
        dropboxEnabledAtMillis = store.getDropboxEnabledAt(),
        lastDropboxSuccessAtMillis = lastDropbox,
        lastUploadFailure = store.getLastUploadFailure(),
        lastManualExportAtMillis = lastManual,
        totalEntries = written.size,
        entriesSinceLastBackup = sinceLastBackup
    )
}

/** The card for a status, or null when there is nothing to show. Copy is placeholder. */
internal fun BackupStatus.toTimelineCard(nowMillis: Long): TimelineCardRowState? {
    val severity = type.severity
    if (severity == BackupSeverity.NONE) return null
    val dismissible = severity != BackupSeverity.HIGH
    val tone = if (dismissible) TimelineCardTone.WARNING else TimelineCardTone.DANGER

    fun card(
        title: TimelineCardText,
        body: TimelineCardText,
        actionLabel: Int,
        action: TimelineCardAction
    ) = TimelineCardRowState(
        id = BACKUP_CARD_ID,
        title = title,
        body = body,
        actionLabel = actionLabel,
        action = action,
        tone = tone,
        isDismissible = dismissible
    )

    return when (this) {
        BackupStatus.Healthy -> null
        BackupStatus.DropboxDisconnected, is BackupStatus.DropboxFailing -> card(
            TimelineCardText(R.string.backup_card_dropbox_stopped_title),
            TimelineCardText(R.string.backup_card_dropbox_stopped_body),
            R.string.backup_card_reconnect,
            TimelineCardAction.ReconnectDropbox
        )
        BackupStatus.DropboxFull -> card(
            TimelineCardText(R.string.backup_card_dropbox_full_title),
            TimelineCardText(R.string.backup_card_dropbox_full_body),
            R.string.backup_card_open_settings,
            TimelineCardAction.OpenSettings
        )
        is BackupStatus.NeverBackedUp -> card(
            TimelineCardText(R.string.backup_card_never_title),
            TimelineCardText(R.string.backup_card_never_body),
            R.string.backup_card_set_up,
            TimelineCardAction.OpenSettings
        )
        is BackupStatus.Stale -> {
            // Stale starts after 30 days, so this is always at least 4.
            val weeks = (TimeUnit.MILLISECONDS.toDays(nowMillis - lastBackupAtMillis) / 7).toInt()
            card(
                TimelineCardText(R.string.backup_card_stale_title, listOf(weeks)),
                TimelineCardText(R.string.backup_card_stale_body),
                R.string.backup_card_export,
                TimelineCardAction.ExportBackup
            )
        }
    }
}
