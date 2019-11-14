package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import dagger.android.AndroidInjection
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.ContainerActivity.Companion.CHANNEL_ID
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler.Companion.ALARM_TYPE_RTC
import org.threeten.bp.LocalDate
import javax.inject.Inject


/**
 *  Receives broadcasts from an alarm and creates notifications
 */
class ReminderReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: EntryRepository

    override fun onReceive(context: Context, intent: Intent) {
        AndroidInjection.inject(this, context)
        Log.d("blerg", "reminder recieved!")

        val blah = repository.entryCount(LocalDate.now())

        val observer = object : Observer<Int> {
            override fun onChanged(count: Int?) {
                if (count != null && count == 0) {
                    Log.d("blerg", "making notif")
                    val openActivityIntent = Intent(context, ContainerActivity::class.java)
                    openActivityIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //set flag to restart/relaunch the app
                    openActivityIntent.putExtra(fromNotification, true)
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        ALARM_TYPE_RTC,
                        openActivityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    //Build notification
                    createLocalNotification(context, pendingIntent)

                    //stop observing
                    blah.removeObserver(this)
                } else {
                    //stop observing
                    blah.removeObserver(this)
                }


            }
        }

        blah.observeForever(observer)
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

    companion object {
        const val fromNotification = "fromNotification"
    }

}