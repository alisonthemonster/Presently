package journal.gratitude.com.gratitudejournal

import androidx.work.Configuration
import androidx.work.WorkManager
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import journal.gratitude.com.gratitudejournal.di.DaggerApplicationComponent
import journal.gratitude.com.gratitudejournal.di.DaggerAwareWorkerFactory
import javax.inject.Inject

open class GratitudeApplication: DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext, this)
    }

    override fun onCreate() {
        super.onCreate()

        configureWorkManager()

        AndroidThreeTen.init(this)

        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/Larsseit-Medium.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build())
                )
                .build())
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