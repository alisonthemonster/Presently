package journal.gratitude.com.gratitudejournal.ui.security

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import javax.inject.Inject

@AndroidEntryPoint
class LockingService : Service() {

    @Inject
    lateinit var settings: PresentlySettings

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (settings.isBiometricsEnabled()) {
            settings.forceLock()
            // Notify widget to lock immediately
            val intent = Intent(this, journal.gratitude.com.gratitudejournal.widget.RandomEntryWidgetReceiver::class.java).apply {
                action = android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            sendBroadcast(intent)
        }
        stopSelf()
    }
}
