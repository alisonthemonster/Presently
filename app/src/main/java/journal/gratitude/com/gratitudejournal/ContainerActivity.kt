package journal.gratitude.com.gratitudejournal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_NOTIFICATION
import journal.gratitude.com.gratitudejournal.util.reminders.AlarmBootReceiver
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver.Companion.fromNotification

class ContainerActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "Presently Gratitude Reminder"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)

        createNotificationChannel()

        val receiver = ComponentName(this, AlarmBootReceiver::class.java)

        packageManager.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )

        intent.extras?.let {
            val cameFromNotification = it.getBoolean(fromNotification, false)
            if (cameFromNotification) {
                val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
                mFirebaseAnalytics.logEvent(CAME_FROM_NOTIFICATION, null)
            }
        }

        NotificationScheduler().setReminderNotification(this)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = description
            channel.enableVibration(true)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

}
