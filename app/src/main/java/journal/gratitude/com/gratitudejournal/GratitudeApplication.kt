package journal.gratitude.com.gratitudejournal

import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import journal.gratitude.com.gratitudejournal.di.DaggerApplicationComponent

class GratitudeApplication: DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerApplicationComponent.factory().create(applicationContext, this)
    }

    override fun onCreate() {

        super.onCreate()

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
}