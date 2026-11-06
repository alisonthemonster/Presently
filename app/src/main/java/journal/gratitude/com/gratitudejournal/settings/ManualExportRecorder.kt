package journal.gratitude.com.gratitudejournal.settings

import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.EXPORTED_DATA
import journal.gratitude.com.gratitudejournal.model.EXPORTING_BACKUP_ERROR

/**
 * Records what a manual export means for backup status and analytics in one place, so the stored
 * timestamp and the event can't drift apart. Events only say where the export was started from;
 * nothing about the journal, the file or the failure is sent.
 */
class ManualExportRecorder(
    private val store: BackupStatusStore,
    private val analytics: AnalyticsLogger
) {

    fun onExported(source: String = SOURCE_SETTINGS) {
        store.recordManualExport()
        analytics.recordEvent(EXPORTED_DATA, mapOf(SOURCE_KEY to source))
    }

    fun onExportFailed(source: String = SOURCE_SETTINGS) {
        analytics.recordEvent(EXPORTING_BACKUP_ERROR, mapOf(SOURCE_KEY to source))
    }

    companion object {
        const val SOURCE_KEY = "source"
        const val SOURCE_SETTINGS = "settings"
    }
}
