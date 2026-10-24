package journal.gratitude.com.gratitudejournal.logging

import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings

/**
 * Whether the user has Dropbox backup on, and how often: `off`, `daily`, `weekly` or
 * `every_change`. Named for the provider so other providers can get their own property
 * alongside it. Set as a user property so any other analytics can be segmented by it.
 */
internal const val DROPBOX_BACKUP_PROPERTY = "dropbox_backup"

internal fun dropboxBackupPropertyValue(dropboxOn: Boolean, cadence: BackupCadence): String {
    if (!dropboxOn) return "off"
    // Exhaustive on purpose: a new cadence must be named here, not reported as another one.
    return when (cadence) {
        BackupCadence.DAILY -> "daily"
        BackupCadence.WEEKLY -> "weekly"
        BackupCadence.EVERY_CHANGE -> "every_change"
    }
}

/** Call at launch and whenever the token or cadence changes. Token changes are handled in [RealPresentlySettings]. */
internal fun AnalyticsLogger.updateDropboxBackupProperty(settings: PresentlySettings) {
    setUserProperty(
        DROPBOX_BACKUP_PROPERTY,
        dropboxBackupPropertyValue(
            dropboxOn = settings.getAccessToken() != null,
            cadence = settings.getAutomaticBackupCadence()
        )
    )
}
