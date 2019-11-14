package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import journal.gratitude.com.gratitudejournal.GratitudeApplication

// Definition of a Dagger component
@Component(modules = [ApplicationModule::class])
interface AppComponent : AndroidInjector<GratitudeApplication> {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance applicationContext: Context): AppComponent
    }

}