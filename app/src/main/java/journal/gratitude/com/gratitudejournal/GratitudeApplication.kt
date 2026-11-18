package journal.gratitude.com.gratitudejournal

import android.app.Application
import com.airbnb.mvrx.Mavericks
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.widget.EntryCountWidgetUpdater

@HiltAndroidApp
class GratitudeApplication : BaseGratitudeApplication()

open class BaseGratitudeApplication : Application(), Configuration.Provider {

    // Hilt test applications cannot use field injection, so use an entry point instead.
    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface ApplicationEarlyEntryPoint {
        fun getWorkerFactory(): HiltWorkerFactory
        fun getEntryCountWidgetUpdater(): EntryCountWidgetUpdater
    }

    override val workManagerConfiguration: Configuration
        get() {
            val earlyEntryPoint = EarlyEntryPoints.get(this, ApplicationEarlyEntryPoint::class.java)
            return Configuration.Builder()
                .setWorkerFactory(earlyEntryPoint.getWorkerFactory())
                .build()
        }

    override fun onCreate() {
        super.onCreate()

        Mavericks.initialize(this)
        EarlyEntryPoints.get(this, ApplicationEarlyEntryPoint::class.java)
            .getEntryCountWidgetUpdater()
            .start()
        // TODO: If Mavericks runtime mocking is needed again, re-enable it only after upgrading
        // to a version that is compatible with Android 13+ dynamic receiver registration rules.
    }
}
