package journal.gratitude.com.gratitudejournal.di

import android.app.Application
import android.content.Context
import com.presently.analytics.wiring.AnalyticsModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import journal.gratitude.com.gratitudejournal.GratitudeApplication
import javax.inject.Singleton

// Definition of a Dagger component
@Singleton
@Component(
    modules = [
        AnalyticsModule::class,
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        AppLockModule::class,
        CelebrateDialogModule::class,
        CloudUploadModule::class,
        EntryModule::class,
        SearchModule::class,
        SettingsModule::class,
        ThemeModule::class,
        TimelineModule::class,
        WorkerModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<GratitudeApplication> {

    @Component.Factory
    interface Factory {
        fun create(
            @BindsInstance applicationContext: Context,
            @BindsInstance application: Application
        ): ApplicationComponent
    }
}