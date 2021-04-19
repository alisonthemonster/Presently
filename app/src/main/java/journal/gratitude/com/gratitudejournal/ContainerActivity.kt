package journal.gratitude.com.gratitudejournal

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.preference.PreferenceManager
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_NOTIFICATION
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.FINGERPRINT
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.THEME_PREF
import journal.gratitude.com.gratitudejournal.util.LocaleHelper
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver.Companion.fromNotification
import java.util.*

class ContainerActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "Presently Gratitude Reminder"
    }

    override fun attachBaseContext(newBase: Context) {
        val context: Context = LocaleHelper.onAppAttached(newBase)
        super.attachBaseContext(context)
        SplitCompat.installActivity(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val currentTheme = sharedPref.getString(THEME_PREF, "original") ?: "original"
        setAppTheme(currentTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.container_activity)

        createNotificationChannel()

        intent.extras?.let {
            val cameFromNotification = it.getBoolean(fromNotification, false)
            if (cameFromNotification) {
                val mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
                mFirebaseAnalytics.logEvent(CAME_FROM_NOTIFICATION, null)
            }
        }

        NotificationScheduler().configureNotifications(this)

        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    override fun onResume() {
        super.onResume()
        isGooglePlayServicesAvailable(this)
    }

    private fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            }
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val fingerprintLock = sharedPref.getBoolean(FINGERPRINT, false)
        if (fingerprintLock) {
            val lastDestroyTime =
                sharedPref.getLong("last_destroy_time", -1L) //check this default makes sense
            val currentTime = Date(System.currentTimeMillis()).time
            val diff = currentTime - lastDestroyTime
            if (diff > 300000L) {
                //if more than 5 minutes (300000ms) have passed since last destroy, lock out user
                val navController = findNavController(R.id.nav_host_fragment)
                val navInflater = navController.navInflater
                val graph = navInflater.inflate(R.navigation.nav_graph)

                graph.startDestination = R.id.appLockFragment

                navController.graph = graph
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(this)
        val fingerprintLock = sharedPref.getBoolean(FINGERPRINT, false)
        if (fingerprintLock) {
            val date = Date(System.currentTimeMillis())
            sharedPref.edit().putLong("last_destroy_time", date.time).apply()
        }
    }

    private fun createNotificationChannel() {
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

    private fun setAppTheme(currentTheme: String) {
        when (currentTheme) {
            "Sunset" -> setTheme(R.style.AppTheme_SUNSET)
            "Moonlight" -> setTheme(R.style.AppTheme_MOONLIGHT)
            "Midnight" -> setTheme(R.style.AppTheme_MIDNIGHT)
            "Ivy" -> setTheme(R.style.AppTheme_IVY)
            "Dawn" -> setTheme(R.style.AppTheme_DAWN)
            "Wesley" -> setTheme(R.style.AppTheme_WESLEY)
            "Moss" -> setTheme(R.style.AppTheme_MOSS)
            "Clean" -> setTheme(R.style.AppTheme_CLEAN)
            "Glacier" -> setTheme(R.style.AppTheme_GLACIER)
            "Gelato" -> setTheme(R.style.AppTheme_GELATO)
            "Waves" -> setTheme(R.style.AppTheme_WAVES)
            "Beach" -> setTheme(R.style.AppTheme_BEACH)
            "Field" -> setTheme(R.style.AppTheme_FIELD)
            "Western" -> setTheme(R.style.AppTheme_WESTERN)
            "Sunlight" -> setTheme(R.style.AppTheme_SUNLIGHT)
            "Tulip" -> setTheme(R.style.AppTheme_TULIP)
            "Rosie" -> setTheme(R.style.AppTheme_ROSIE)
            "Daisy" -> setTheme(R.style.AppTheme_DAISY)
            "Matisse" -> setTheme(R.style.AppTheme_MATISSE)
            "Clouds" -> setTheme(R.style.AppTheme_CLOUDS)
            "Monstera" -> setTheme(R.style.AppTheme_MONSTERA)
            "Lotus" -> setTheme(R.style.AppTheme_LOTUS)
            "Katie" -> setTheme(R.style.AppTheme_KATIE)
            "Brittany" -> setTheme(R.style.AppTheme_BRITTANY)
            "Jungle" -> setTheme(R.style.AppTheme_JUNGLE)
            "Julie" -> setTheme(R.style.AppTheme_JULIE)
            "Ellen" -> setTheme(R.style.AppTheme_ELLEN)
            "Danah" -> setTheme(R.style.AppTheme_DANAH)
            "Ahalya" -> setTheme(R.style.AppTheme_AHALYA)
            "Whale" -> setTheme(R.style.AppTheme_WHALE)
            else -> setTheme(R.style.Base_AppTheme)
        }
    }
}
