package journal.gratitude.com.gratitudejournal

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.splitcompat.SplitCompat
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.di.SettingsEntryPoint
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_NOTIFICATION
import journal.gratitude.com.gratitudejournal.util.LocaleHelper
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //todo test theme is preserved when they get this update
    //todo status bar color
    //todo test dropbox backup still works (esp the every change option)

    @Inject lateinit var settings: PresentlySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameFromNotification = intent.extras?.getBoolean(ReminderReceiver.fromNotification, false)
            ?: false

        setContent {
            PresentlyContainer(cameFromNotification)
        }

        createNotificationChannels()

        NotificationScheduler().configureNotifications(this, settings)
    }

    override fun attachBaseContext(newBase: Context) {
        val settings = EntryPointAccessors.fromApplication(newBase, SettingsEntryPoint::class.java).settings
        val context: Context = LocaleHelper.onAppAttached(newBase, settings)
        super.attachBaseContext(context)
        SplitCompat.installActivity(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(ContainerActivity.CHANNEL_ID, getString(R.string.channel_name),  NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = getString(R.string.channel_description)
            notificationChannel.enableVibration(true)

            val backupChannel = NotificationChannel(ContainerActivity.BACKUP_STATUS_CHANNEL, getString(R.string.backup_channel_name), NotificationManager.IMPORTANCE_HIGH)
            backupChannel.description = getString(R.string.backup_channel_description)
            backupChannel.enableVibration(true)

            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannels(listOf(notificationChannel, backupChannel))

        }
    }

}