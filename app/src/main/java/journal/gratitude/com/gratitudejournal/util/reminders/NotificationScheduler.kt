package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import androidx.preference.PreferenceManager
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.model.CANCELLED_NOTIFS
import org.threeten.bp.LocalTime
import java.util.*


class NotificationScheduler {

    companion object {
        const val ALARM_TYPE_RTC = 100
        const val PENDING_INTENT = 101
    }

    private var alarmManager: AlarmManager? = null
    private lateinit var alarmIntent: PendingIntent

    fun setReminderNotification(context: Context) {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)

        val alarmIsOn = sharedPref.getBoolean("notif_parent", true)

        if (alarmIsOn) {

            val prefTime = sharedPref.getString("pref_time", "21:00")

            val alarmTime = LocalTime.parse(prefTime)

            val intent = Intent(context, ReminderReceiver::class.java)
            alarmIntent = intent.let {
                PendingIntent.getBroadcast(
                    context,
                    PENDING_INTENT,
                    it,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ) //TODO check flag
            }

            val alarmTimeCal = if (LocalTime.now().isAfter(alarmTime)) {
                //today's alarm already happened use start the next one tomorrow
                Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                    set(Calendar.MINUTE, alarmTime.minute)
                    add(Calendar.DATE,1)
                }
            } else {
                Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, alarmTime.hour)
                    set(Calendar.MINUTE, alarmTime.minute)
                }
            }

            alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager?.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                alarmTimeCal.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                alarmIntent
            )
        } else {
            firebaseAnalytics.logEvent(CANCELLED_NOTIFS, null)
            cancelNotifications(context)
        }
    }

    private fun cancelNotifications(context: Context) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = intent.let {
            PendingIntent.getBroadcast(context,
                PENDING_INTENT, it, 0)
        }

        pendingIntent.cancel()

        alarmManager?.cancel(pendingIntent)

    }
}