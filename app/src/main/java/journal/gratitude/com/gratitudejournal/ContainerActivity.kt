package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.intSetOf
import androidx.core.view.WindowCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_NOTIFICATION
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_WIDGET
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.security.AppLockFragment
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.AppLocaleManager
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver.Companion.fromNotification
import journal.gratitude.com.gratitudejournal.widget.GratitudeQuoteWidget
import journal.gratitude.com.gratitudejournal.widget.GratitudeQuoteWidgetReceiver
import journal.gratitude.com.gratitudejournal.widget.RandomEntryWidget
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
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
    @Inject lateinit var repository: EntryRepository
    @Inject lateinit var crashReporter: CrashReporter

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

        logLaunchSourceFromIntent(intent)

        NotificationScheduler().configureNotifications(this, settings)

        maybePublishWidgetPreview()

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
        logLaunchSourceFromIntent(intent)
        handleWidgetIntent(intent)

        if (!settings.isBiometricsEnabled() || !settings.shouldLockApp()) {
            val extras = intent.extras
            if (extras?.getString(NOTIFICATION_SCREEN_EXTRA) == WIDGET_ENTRY_SCREEN) {
                val date = extras.getString(RandomEntryWidget.EXTRA_SELECTED_DATE)
                if (date != null) {
                    // Ensure the Timeline is the base fragment
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.container_fragment, TimelineFragment.newInstance())
                        .commit()
                    // Then navigate to the specific entry
                    navigateToEntry(date)
                }
            }
        }
    }

    private fun logLaunchSourceFromIntent(intent: Intent) {
        val extras = intent.extras ?: return
        if (extras.getBoolean(fromNotification, false)) {
            analyticsLogger.recordEvent(CAME_FROM_NOTIFICATION)
        }
        if (extras.getBoolean(GratitudeQuoteWidget.cameFromWidgetKey.name, false)) {
            analyticsLogger.recordEvent(CAME_FROM_WIDGET)
        }
    }

    private fun maybePublishWidgetPreview() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) return
        val componentName = ComponentName(this, GratitudeQuoteWidgetReceiver::class.java)
        val providerInfo = AppWidgetManager.getInstance(this).installedProviders
            .firstOrNull { it.provider == componentName }
        val alreadyPublished = (providerInfo?.generatedPreviewCategories ?: 0) and
            AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN != 0
        if (alreadyPublished) return
        lifecycleScope.launch {
            GlanceAppWidgetManager(this@ContainerActivity)
                .setWidgetPreviews(
                    GratitudeQuoteWidgetReceiver::class,
                    intSetOf(AppWidgetProviderInfo.WIDGET_CATEGORY_HOME_SCREEN)
                )
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
            startService(Intent(this, journal.gratitude.com.gratitudejournal.ui.security.LockingService::class.java))

            if (settings.shouldLockApp()) {
                val fragment = AppLockFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.container_fragment, fragment)
                    .commit()
            } else {
                // Reset the timer so the widget doesn't lock while we're using the app.
                settings.setOnPauseTime()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (settings.isBiometricsEnabled()) {
            if (isFinishing) {
                settings.forceLock()
            } else {
                settings.setOnPauseTime()
            }
        }

        // Notify widget to update so it shows a fresh entry next time the user sees it
        lifecycleScope.launch {
            RandomEntryWidget().updateAll(this@ContainerActivity)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settings.isBiometricsEnabled()) {
            settings.forceLock()
        }
        lifecycleScope.launch {
            RandomEntryWidget().updateAll(this@ContainerActivity)
        }
    }

    private fun handleWidgetIntent(intent: Intent) {
        val extras = intent.extras ?: return
        if (extras.containsKey(RandomEntryWidget.selectedDateKey.name)) {
            val date = extras.getString(RandomEntryWidget.selectedDateKey.name)
            if (date != null) {
                intent.putExtra(NOTIFICATION_SCREEN_EXTRA, WIDGET_ENTRY_SCREEN)
                intent.putExtra(RandomEntryWidget.EXTRA_SELECTED_DATE, date)
            }
        }
    }

    fun navigateToEntry(date: String) {
        try {
            val localDate = LocalDate.parse(date)
            lifecycleScope.launch {
                val entries = repository.getEntries()
                val numEntries = entries.size
                val entry = entries.find { it.entryDate == localDate }

                val fragment = EntryFragment.newInstance(
                    date = localDate,
                    numEntries = numEntries,
                    isNewEntry = entry == null,
                    resources = resources
                )
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container_fragment, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        } catch (e: Exception) {
            crashReporter.logHandledException(e)
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
