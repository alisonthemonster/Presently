package journal.gratitude.com.gratitudejournal.logging

import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings

/**
 * Whether the user has Dropbox backup on, and how often: `off`, `daily`, `weekly` or
 * `every_change`. Named for the provider so other providers can get their own property
 * alongside it. Set as a user property so any other analytics can be segmented by it.
 */
internal const val DROPBOX_BACKUP_PROPERTY = "dropbox_backup"

internal fun dropboxBackupPropertyValue(dropboxOn: Boolean, cadence: BackupCadence): String =
    when {
        !dropboxOn -> "off"
        cadence == BackupCadence.DAILY -> "daily"
        cadence == BackupCadence.WEEKLY -> "weekly"
        else -> "every_change"
    }

/** Call at launch and whenever the token or cadence changes. */
internal fun AnalyticsLogger.updateDropboxBackupProperty(settings: PresentlySettings) {
    setUserProperty(
        DROPBOX_BACKUP_PROPERTY,
        dropboxBackupPropertyValue(
            dropboxOn = settings.getAccessToken() != null,
            cadence = settings.getAutomaticBackupCadence()
        )
    )
}
