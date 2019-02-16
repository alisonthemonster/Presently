package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.ContainerActivity.Companion.CHANNEL_ID
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler.Companion.ALARM_TYPE_RTC

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val intentToRepeat = Intent(context, ContainerActivity::class.java)
        intentToRepeat.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //set flag to restart/relaunch the app
        val pendingIntent = PendingIntent.getActivity(
            context,
            ALARM_TYPE_RTC,
            intentToRepeat,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        //Build notification
        createLocalNotification(context, pendingIntent)

        //TODO check intent action

    }

    private fun createLocalNotification(context: Context, pendingIntent: PendingIntent) {

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Presently Gratitude Reminder")
            .setContentText("What are you grateful for today?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true) //removes notif when tapped


        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(ALARM_TYPE_RTC, notificationBuilder.build())
    }

}