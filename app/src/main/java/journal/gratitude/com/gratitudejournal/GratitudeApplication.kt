package journal.gratitude.com.gratitudejournal

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.work.Configuration
import androidx.work.WorkManager
import com.airbnb.mvrx.mocking.MockableMavericks
import com.google.android.play.core.splitcompat.SplitCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import journal.gratitude.com.gratitudejournal.di.ApplicationComponent
import journal.gratitude.com.gratitudejournal.di.DaggerApplicationComponent
import journal.gratitude.com.gratitudejournal.di.DaggerAwareWorkerFactory
import javax.inject.Inject

open class GratitudeApplication: DaggerApplication() {

    lateinit var appComponent: ApplicationComponent

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        appComponent = DaggerApplicationComponent.factory().create(applicationContext, this)
        return appComponent
    }

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