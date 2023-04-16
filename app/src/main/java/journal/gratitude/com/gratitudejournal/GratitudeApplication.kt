package journal.gratitude.com.gratitudejournal

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.play.core.splitcompat.SplitCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@HiltAndroidApp
class GratitudeApplication : BaseGratitudeApplication()

open class BaseGratitudeApplication: Application() {

    // Hilt test applications cannot use field injection, so you an entry point instead
    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface ApplicationEarlyEntryPoint {
        fun getWorkerFactory(): HiltWorkerFactory
    }


    override fun onCreate() {
        super.onCreate()

        configureWorkManager()

        AndroidThreeTen.init(this)
    }

    private fun configureWorkManager() {
        val earlyEntryPoint = EarlyEntryPoints.get(this, ApplicationEarlyEntryPoint::class.java)
        val daggerAwareWorkerFactory = earlyEntryPoint.getWorkerFactory()
        val config = Configuration.Builder()
            .setWorkerFactory(daggerAwareWorkerFactory)
            .build()
        WorkManager.initialize(this, config)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Emulates installation of future on demand modules using SplitCompat.
        SplitCompat.install(this)
    }

}