package journal.gratitude.com.gratitudejournal.util.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmBootReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action.equals("android.intent.action.BOOT_COMPLETED")) {
            val notificationScheduler =
                NotificationScheduler()
            notificationScheduler.setReminderNotification(context)
        }
    }
}