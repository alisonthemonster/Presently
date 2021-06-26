package journal.gratitude.com.gratitudejournal.ui

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.ApplicationModule
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [ApplicationModule::class]
)
object FakeApplicationModule {

    @Provides
    fun provideRepository(): EntryRepository = FakeEntryRepository()
}