package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import com.presently.settings.PresentlySettings
import org.threeten.bp.LocalTime
import java.util.*

/**
 * Schedules or cancels broadcasts to the ReminderReceiver at the specified time
 *
 */
class NotificationScheduler {

    companion object {
        const val ALARM_TYPE_RTC = 100
        const val PENDING_INTENT = 101 //used for old repeating alarms
        const val EXACT_ALARM_REQUEST_CODE = 102 //new request code for exact alarms
    }

    // Called to ensure the notification is properly scheduled or cancelled
    fun configureNotifications(context: Context, settings: PresentlySettings) {
        val hasNotificationsOn = settings.hasEnabledNotifications()
        val hasDisabledAlarmReminders = settings.hasUserDisabledAlarmReminders(context)
        if (hasNotificationsOn && !hasDisabledAlarmReminders) {
            val alarmTime = settings.getNotificationTime()
            setNotificationTime(context, alarmTime)
        } else {
            disableNotifications(context)
        }
    }

    //sets the exact alarm, that when triggered will send the notification
    fun setNotificationTime(context: Context, alarmTime: LocalTime) {
        //cancel the old repeating alarm if present (we want to only use exact alarms from now on)
        cancelOldAlarms(context)

        val alarmIntent = PendingIntent.getBroadcast(
                context,
                EXACT_ALARM_REQUEST_CODE,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        val alarmTimeCal = if (LocalTime.now().isAfter(alarmTime)) {
            //today's alarm already happened use start the next one tomorrow
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
                add(Calendar.DATE, 1)
            }
        } else {
            Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                set(Calendar.MINUTE, alarmTime.minute)
            }
        }

        // Enable the reboot/timechange broadcast receiver
        // Programmatically enabling the receiver overrides the manifest setting, even across reboots
        val receiver = ComponentName(context, NotificationResetReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            alarmTimeCal.timeInMillis,
            alarmIntent
        )
    }

    fun disableNotifications(context: Context) {
        cancelNotifications(context)
        disableResetReceiver(context)
    }

    //cancels any existing notifications
    private fun cancelNotifications(context: Context) {
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                EXACT_ALARM_REQUEST_CODE,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        pendingIntent.cancel()
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private fun disableResetReceiver(context: Context) {
        // disable the reboot/timechange broadcast receiver
        val receiver = ComponentName(context, NotificationResetReceiver::class.java)
        context.packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun cancelOldAlarms(context: Context) {
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                PENDING_INTENT,
                Intent(context, ReminderReceiver::class.java),
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

        pendingIntent.cancel()
        val alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}