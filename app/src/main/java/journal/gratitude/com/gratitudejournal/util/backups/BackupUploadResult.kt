package journal.gratitude.com.gratitudejournal.util.backups

sealed class BackupUploadResult
data object BackupUploadSuccess : BackupUploadResult()
data class BackupUploadFailure(val exception: Exception) : BackupUploadResult()
data class BackupAuthFailure(val exception: Exception) : BackupUploadResult()
data class BackupStorageFullFailure(val exception: Exception) : BackupUploadResult()
