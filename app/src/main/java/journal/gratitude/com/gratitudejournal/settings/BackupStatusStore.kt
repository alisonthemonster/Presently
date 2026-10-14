package journal.gratitude.com.gratitudejournal.settings

import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailureType

/**
 * Persisted facts about backups, used to resolve the backup status shown to the user.
 * The store stamps events with the current time itself. Timestamps are epoch millis.
 */
interface BackupStatusStore {

    fun getLastDropboxSuccessAt(): Long?

    fun getLastManualExportAt(): Long?

    fun getLastUploadFailure(): UploadFailure?

    /** The baseline for the overdue check: when Dropbox was connected, or first seen running. */
    fun getDropboxEnabledAt(): Long?

    fun wasDropboxDisconnectedUnexpectedly(): Boolean

    /** Records a successful upload and clears any recorded failure. */
    fun recordDropboxUploadSuccess()

    /** Records a failed upload, without letting a transient failure hide an unresolved one. */
    fun recordDropboxUploadFailure(type: UploadFailureType)

    fun recordManualExport()

    /**
     * Gives users who connected before backup status existed a baseline. Does nothing if one
     * already exists, so it can be called on every run.
     */
    fun seedDropboxBaselineIfMissing()

    /** The user connected Dropbox: resets the baseline and clears any earlier problem. */
    fun recordDropboxConnected()

    /** The user turned Dropbox off. Keeps the last success so past backups still count. */
    fun recordDropboxDisconnectedByUser()

    /** Dropbox access was lost without the user asking (revoked or expired token). */
    fun markDropboxDisconnectedUnexpectedly()
}
