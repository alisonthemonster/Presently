package journal.gratitude.com.gratitudejournal.di

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.airbnb.mvrx.MavericksViewModel
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import journal.gratitude.com.gratitudejournal.GratitudeApplication
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        ApplicationModule::class,
        TimelineModule::class,
        EntryModule::class,
        SearchModule::class,
        SettingsModule::class,
        CloudUploadModule::class,
        WorkerModule::class,
        AndroidSupportInjectionModule::class
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

    fun viewModelFactories(): Map<Class<out MavericksViewModel<*>>, AssistedViewModelFactory<*, *>>
}