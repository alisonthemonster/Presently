package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupPreferences: BackupPreferences
) {

    fun schedule(provider: BackupProvider, frequency: BackupFrequency) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelAllWorkByTag(provider.workerTag)

        val scheduleConfig = scheduleConfigFor(frequency)
        if (scheduleConfig == null) {
            workManager.cancelUniqueWork(provider.uniqueWorkName)
            return
        }

        workManager.enqueueUniquePeriodicWork(
            provider.uniqueWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            buildPeriodicRequest(provider, scheduleConfig.repeatInterval, scheduleConfig.timeUnit)
        )
    }

    fun cancel(provider: BackupProvider) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(provider.uniqueWorkName)
        workManager.cancelAllWorkByTag(provider.workerTag)
    }

    fun triggerOnEveryChangeBackups() {
        BackupProvider.entries
            .filter { backupPreferences.getState(it).isConnected }
            .filter { backupPreferences.getState(it).frequency == BackupFrequency.ON_EVERY_CHANGE }
            .forEach { provider ->
                WorkManager.getInstance(context).enqueue(
                    when (provider) {
                        BackupProvider.DROPBOX ->
                            OneTimeWorkRequestBuilder<DropboxBackupWorker>()
                        BackupProvider.GOOGLE_DRIVE ->
                            OneTimeWorkRequestBuilder<GoogleDriveBackupWorker>()
                    }.addTag(provider.workerTag).build()
                )
            }
    }

    private fun buildPeriodicRequest(
        provider: BackupProvider,
        repeatInterval: Long,
        timeUnit: TimeUnit
    ): PeriodicWorkRequest {
        return when (provider) {
            BackupProvider.DROPBOX ->
                PeriodicWorkRequestBuilder<DropboxBackupWorker>(repeatInterval, timeUnit)
            BackupProvider.GOOGLE_DRIVE ->
                PeriodicWorkRequestBuilder<GoogleDriveBackupWorker>(repeatInterval, timeUnit)
        }.addTag(provider.workerTag).build()
    }

    internal fun scheduleConfigFor(frequency: BackupFrequency): BackupScheduleConfig? {
        return when (frequency) {
            BackupFrequency.DAILY -> BackupScheduleConfig(1, TimeUnit.DAYS)
            BackupFrequency.MONTHLY -> BackupScheduleConfig(30, TimeUnit.DAYS)
            BackupFrequency.ON_EVERY_CHANGE -> null
        }
    }
}

data class BackupScheduleConfig(
    val repeatInterval: Long,
    val timeUnit: TimeUnit
)
