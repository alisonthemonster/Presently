package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_NOTIFICATION
import journal.gratitude.com.gratitudejournal.ui.security.AppLockFragment
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.util.AppLocaleManager
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver.Companion.fromNotification
import journal.gratitude.com.gratitudejournal.widget.RandomEntryWidget
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import javax.inject.Inject

@AndroidEntryPoint
class ContainerActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "Presently Gratitude Reminder"
        const val BACKUP_STATUS_CHANNEL = "Presently Automatic Backup Status"
        const val NOTIFICATION_SCREEN_EXTRA = "NOTIFICATION_EXTRA"

        // Custom screen token for widget navigation
        const val WIDGET_ENTRY_SCREEN = "WidgetEntry"
    }

    @Inject lateinit var settings: PresentlySettings
    @Inject lateinit var analyticsLogger: AnalyticsLogger

    override fun attachBaseContext(newBase: Context) {
        AppLocaleManager.applyStoredApplicationLocales(newBase)
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentTheme = settings.getCurrentTheme()
        setAppTheme(currentTheme)
        setContentView(R.layout.container_activity)

        createNotificationChannels()

        intent.extras?.let {
            val cameFromNotification = it.getBoolean(fromNotification, false)
            if (cameFromNotification) {
                analyticsLogger.recordEvent(CAME_FROM_NOTIFICATION)
            }
        }

        NotificationScheduler().configureNotifications(this, settings)

        if (resources.configuration.orientation != Configuration.ORIENTATION_LANDSCAPE) {
            //lays app behind system bars
                //not in landscape mode so navigation bar doesn't block UI
            WindowCompat.setDecorFitsSystemWindows(window, false)
        }

        handleWidgetIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        isGooglePlayServicesAvailable(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent?) {
        if (intent?.action == RandomEntryWidget.ACTION_OPEN_ENTRY) {
            val selectedDate = intent.getStringExtra(RandomEntryWidget.EXTRA_SELECTED_DATE)
            if (!selectedDate.isNullOrEmpty()) {
                if (settings.isBiometricsEnabled() && settings.shouldLockApp()) {
                    // Inject routing destinations directly into intent data so AppLockFragment can read them
                    intent.putExtra(NOTIFICATION_SCREEN_EXTRA, WIDGET_ENTRY_SCREEN)
                    intent.putExtra(RandomEntryWidget.EXTRA_SELECTED_DATE, selectedDate)
                } else {
                    // Safe to navigate directly if the app isn't locked right now
                    window.decorView.post {
                        navigateToEntry(selectedDate)
                    }
                }
            }
        }
    }

    fun navigateToEntry(dateString: String) {
        try {
            val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
            val localDate = LocalDate.parse(dateString, formatter)

            val fragment = EntryFragment.newInstance(
                date = localDate,
                numEntries = 0,
                isNewEntry = false,
                resources = resources
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.container_fragment, fragment)
                .addToBackStack(null)
                .commit()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404)?.show()
            }
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        val isBiometricsEnabled = settings.isBiometricsEnabled()
        if (isBiometricsEnabled) {
            if (settings.shouldLockApp()) {
                val fragment = AppLockFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container_fragment, fragment)
                    .commit()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (settings.isBiometricsEnabled()) {
            settings.setOnPauseTime()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = getString(R.string.channel_description)
            notificationChannel.enableVibration(true)

            val backupChannel = NotificationChannel(BACKUP_STATUS_CHANNEL, getString(R.string.backup_channel_name), NotificationManager.IMPORTANCE_HIGH)
            backupChannel.description = getString(R.string.backup_channel_description)
            backupChannel.enableVibration(true)

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannels(listOf(notificationChannel, backupChannel))
        }
    }

    private fun setAppTheme(currentTheme: String) {
        val themeSpec = PresentlyThemeSpec.fromStorageValue(currentTheme)
        setTheme(themeSpec.styleRes)
    }
}
