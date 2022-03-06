package journal.gratitude.com.gratitudejournal.util.backups

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.ListenableWorker
import com.dropbox.core.InvalidAccessTokenException
import com.dropbox.core.v2.files.UploadErrorException
import com.presently.coroutine_utils.AppCoroutineDispatchers
import com.presently.logging.CrashReporter
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.security.AppLockFragment.Companion.SETTINGS_SCREEN
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.CloudProvider
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import javax.inject.Inject

/**
 * A class that will handle the backup upload to the cloud. It also handles any errors that
 * the cloud provider may return.
 *
 */
class RealUploader @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val repository: EntryRepository,
    private val cloudProvider: CloudProvider,
    private val crashReporter: CrashReporter,
    private val settings: PresentlySettings
) : Uploader {
    override suspend fun uploadEntries(appContext: Context): ListenableWorker.Result {
        //get items
        val items = repository.getEntries()

        //do not backup data if there is nothing to back up
        if (items.isEmpty()) {
            return ListenableWorker.Result.success()
        }

        //create temp file
        val file = withContext(dispatchers.io) {
            File.createTempFile(
                "tempPresentlyBackup",
                null,
                appContext.cacheDir
            )
        }

        //create csv
        val fileExporter = FileExporter(FileWriter(file), dispatchers)

        val csvResult = when (val csvResult = fileExporter.exportToCSV(items, file)) {
            is CsvFileCreated -> {
                //upload to cloud
                when (val result = cloudProvider.uploadToCloud(csvResult.file)) {
                    is UploadError -> {
                        if (result.exception is InvalidAccessTokenException) {
                            sendDropboxAuthFailureNotification(appContext)
                            DropboxUploader.deauthorizeDropboxAccess(appContext, settings, dispatchers.io)
                        }
                        if (result.exception.isInsufficientSpace()) {
                            sendDropboxTooFullNotification(appContext)
                        }

                        crashReporter.logHandledException(result.exception)
                        ListenableWorker.Result.failure()
                    }
                    is UploadSuccess -> ListenableWorker.Result.success()
                }
            }
            is CsvFileError -> ListenableWorker.Result.failure()
        }

        //delete temp file
        file.delete()

        return csvResult
    }

    private fun sendDropboxTooFullNotification(appContext: Context) {
        val helpPageIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://help.dropbox.com/accounts-billing/space-storage/over-storage-limit")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val helpPagePendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, helpPageIntent, 0)

        val accountPageIntent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.dropbox.com/account/plan")
            ).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val accountPagePendingIntent: PendingIntent =
            PendingIntent.getActivity(appContext, 0, accountPageIntent, 0)

        val notificationBodyText = appContext.getString(R.string.dropbox_too_full_notif_body)

        val builder = NotificationCompat.Builder(
            appContext,
            ContainerActivity.BACKUP_STATUS_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(appContext.getString(R.string.backup_failure_notif_header))
            .setContentText(notificationBodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(accountPagePendingIntent) //open dropbox account settings when tapped
            .addAction(
                R.drawable.ic_faq,
                appContext.getString(R.string.learn_more),
                helpPagePendingIntent
            ) //open dropbox help page
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(appContext)
        notificationManager.notify(BACKUP_NOTIFICATION_ID, builder.build())
    }

    private fun sendDropboxAuthFailureNotification(appContext: Context) {
        //Launch the settings screen
        val intent = Intent(appContext, ContainerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        intent.putExtra(ContainerActivity.NOTIFICATION_SCREEN_EXTRA, SETTINGS_SCREEN)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notifBody = appContext.getString(R.string.dropbox_sync_error_notif_body)
        val builder = NotificationCompat.Builder(
            appContext,
            ContainerActivity.BACKUP_STATUS_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(appContext.getString(R.string.backup_failure_notif_header))
            .setContentText(notifBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notifBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false) //removed when user re-auths

        val notificationManager = NotificationManagerCompat.from(appContext)
        notificationManager.notify(BACKUP_NOTIFICATION_ID, builder.build())
    }


    private fun Exception.isInsufficientSpace(): Boolean {
        return this is UploadErrorException && this.userMessage.text.contains("insufficient_space")
    }

    companion object {
        val BACKUP_NOTIFICATION_ID = 98104
    }

}

interface Uploader {
    suspend fun uploadEntries(appContext: Context): ListenableWorker.Result
}