package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import javax.inject.Singleton

@Module
class TestApplicationModule {

    @Singleton
    @Provides
    fun provideRepository(): EntryRepository =
        FakeEntryRepository()

}
