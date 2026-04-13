package journal.gratitude.com.gratitudejournal.util.backups

import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import java.io.File

interface CloudBackupProvider {
    val provider: BackupProvider

    suspend fun uploadToCloud(file: File): BackupUploadResult

    suspend fun disconnect()
}
