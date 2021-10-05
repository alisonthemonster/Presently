package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Receives broadcasts when the user changes the alarm & reminders permission for
 * Presently.
 */
@AndroidEntryPoint
class AlarmPermissionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settings: PresentlySettings

    override fun onReceive(context: Context, intent: Intent) {
        //permission for our exact alarm has changed
        if (intent.action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            //update settings with latest permission
            settings.onAlarmPermissionChange(context)

            //reconfigure the notification scheduling with latest permission
            val notificationScheduler = NotificationScheduler()
            notificationScheduler.configureNotifications(context, settings)
        }
    }


}