package journal.gratitude.com.gratitudejournal.ui.security

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.widget.RandomEntryWidget
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
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
            MainScope().launch {
                RandomEntryWidget.updateLockState(this@LockingService, true)
            }
        }
    }
}
