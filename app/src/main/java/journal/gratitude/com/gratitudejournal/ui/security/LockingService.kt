package journal.gratitude.com.gratitudejournal.ui.security

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.widget.RandomEntryWidget
import javax.inject.Inject

@AndroidEntryPoint
class LockingService : Service() {

    @Inject
    lateinit var settings: PresentlySettings

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (settings.isBiometricsEnabled()) {
            settings.forceLock()
            // Notify widget to lock immediately
            val refreshIntent = Intent(this, RandomEntryWidget::class.java).apply {
                action = RandomEntryWidget.ACTION_REFRESH
            }
            sendBroadcast(refreshIntent)
        }
        stopSelf()
    }
}
