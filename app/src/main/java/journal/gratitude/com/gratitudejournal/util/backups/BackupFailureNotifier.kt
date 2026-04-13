package journal.gratitude.com.gratitudejournal.util.backups

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import journal.gratitude.com.gratitudejournal.ui.security.AppLockFragment.Companion.SETTINGS_SCREEN
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupFailureNotifier @Inject constructor() {

    fun notifyAuthFailure(appContext: Context, provider: BackupProvider) {
        val intent = Intent(appContext, ContainerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(ContainerActivity.NOTIFICATION_SCREEN_EXTRA, SETTINGS_SCREEN)
        }
        val pendingIntent = PendingIntent.getActivity(
            appContext,
            provider.notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBody = appContext.getString(
            when (provider) {
                BackupProvider.DROPBOX -> R.string.dropbox_sync_error_notif_body
                BackupProvider.GOOGLE_DRIVE -> R.string.google_drive_sync_error_notif_body
            }
        )

        val builder = NotificationCompat.Builder(
            appContext,
            ContainerActivity.BACKUP_STATUS_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(appContext.getString(R.string.backup_failure_notif_header))
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)

        NotificationManagerCompat.from(appContext).notify(provider.notificationId, builder.build())
    }

    fun notifyStorageFull(appContext: Context, provider: BackupProvider) {
        val accountPageIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                when (provider) {
                    BackupProvider.DROPBOX -> "https://www.dropbox.com/account/plan"
                    BackupProvider.GOOGLE_DRIVE -> "https://one.google.com/storage"
                }
            )
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val helpPageIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(
                when (provider) {
                    BackupProvider.DROPBOX -> "https://help.dropbox.com/accounts-billing/space-storage/over-storage-limit"
                    BackupProvider.GOOGLE_DRIVE -> "https://support.google.com/googleone/answer/9776477"
                }
            )
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val accountPagePendingIntent = PendingIntent.getActivity(
            appContext,
            provider.notificationId,
            accountPageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val helpPagePendingIntent = PendingIntent.getActivity(
            appContext,
            provider.notificationId + 1000,
            helpPageIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBody = appContext.getString(
            when (provider) {
                BackupProvider.DROPBOX -> R.string.dropbox_too_full_notif_body
                BackupProvider.GOOGLE_DRIVE -> R.string.google_drive_too_full_notif_body
            }
        )

        val builder = NotificationCompat.Builder(
            appContext,
            ContainerActivity.BACKUP_STATUS_CHANNEL
        )
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(appContext.getString(R.string.backup_failure_notif_header))
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(accountPagePendingIntent)
            .addAction(
                R.drawable.ic_faq,
                appContext.getString(R.string.learn_more),
                helpPagePendingIntent
            )
            .setAutoCancel(true)

        NotificationManagerCompat.from(appContext).notify(provider.notificationId, builder.build())
    }

    fun cancel(provider: BackupProvider, context: Context) {
        NotificationManagerCompat.from(context).cancel(provider.notificationId)
    }
}
