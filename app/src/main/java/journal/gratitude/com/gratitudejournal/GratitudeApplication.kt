package journal.gratitude.com.gratitudejournal

import android.app.Application
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.work.Configuration
import androidx.work.WorkManager
import com.airbnb.mvrx.mocking.MockableMavericks
import com.google.android.play.core.splitcompat.SplitCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.HiltAndroidApp
import journal.gratitude.com.gratitudejournal.di.ApplicationComponent
import journal.gratitude.com.gratitudejournal.di.DaggerAwareWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
open class GratitudeApplication: Application() {

    lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()

        configureWorkManager()

        AndroidThreeTen.init(this)

        MockableMavericks.initialize(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Emulates installation of future on demand modules using SplitCompat.
        SplitCompat.install(this)
    }

    @Inject
    lateinit var daggerAwareWorkerFactory: DaggerAwareWorkerFactory

    private fun configureWorkManager() {
        val config = Configuration.Builder()
            .setWorkerFactory(daggerAwareWorkerFactory)
            .build()
        WorkManager.initialize(this, config)
    }
}

fun ComponentActivity.appComponent(): ApplicationComponent {
    return (application as GratitudeApplication).appComponent
}