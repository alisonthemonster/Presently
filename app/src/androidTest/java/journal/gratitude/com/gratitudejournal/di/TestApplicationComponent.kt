package journal.gratitude.com.gratitudejournal.di

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import javax.inject.Singleton

@Singleton
@Component(modules = [
    TestApplicationModule::class,
    TimelineModule::class,
    EntryModule::class,
    SearchModule::class,
    SettingsModule::class,
    TestCloudUploadModule::class,
    WorkerModule::class,
    AndroidSupportInjectionModule::class])
interface TestApplicationComponent : AndroidInjector<TestGratitudeApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context,
                   @BindsInstance application: Application
        ): TestApplicationComponent
    }


    val entryRepository: EntryRepository
}