package journal.gratitude.com.gratitudejournal.fakes

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.ApplicationModule
import journal.gratitude.com.gratitudejournal.di.RepositoryModule
import journal.gratitude.com.gratitudejournal.fakes.FakeEntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import javax.inject.Singleton

/**
 * EntryRepository binding to use in tests.
 *
 * Hilt will inject a [FakeEntryRepository] instead of a [EntryRepositoryImpl].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
abstract class FakeRepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepository(repo: FakeEntryRepository): EntryRepository
}