package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.MainActivity.Companion.CHANNEL_ID
import journal.gratitude.com.gratitudejournal.MainActivity.Companion.INITIAL_SCREEN
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.navigation.UserStartDestination
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler.Companion.ALARM_TYPE_RTC
import javax.inject.Inject

/**
 *  Receives broadcasts from an alarm and creates notifications
 */
@AndroidEntryPoint
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settings: PresentlySettings

    override fun onReceive(context: Context, intent: Intent) {
        val openActivityIntent = Intent(context, MainActivity::class.java)
        openActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // set flag to restart/relaunch the app
        openActivityIntent.putExtra(INITIAL_SCREEN, UserStartDestination.ENTRY_SCREEN)
        val pendingIntent = PendingIntent.getActivity(
            context,
            ALARM_TYPE_RTC,
            openActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        createLocalNotification(context, pendingIntent)

        // Schedule tomorrows alarm
        NotificationScheduler().configureNotifications(context, settings)
    }

    private fun createLocalNotification(context: Context, pendingIntent: PendingIntent) {
        val title = context.getString(R.string.reminder_title)
        val content = context.getString(R.string.what_are_you_thankful_for_today)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setCategory(Notification.CATEGORY_REMINDER)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(ALARM_TYPE_RTC, notificationBuilder.build())
    }
}
