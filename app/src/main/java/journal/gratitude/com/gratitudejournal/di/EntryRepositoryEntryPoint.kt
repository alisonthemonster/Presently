package journal.gratitude.com.gratitudejournal.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface EntryRepositoryEntryPoint {
    fun entryRepository(): EntryRepository
    fun coroutineDispatchers(): AppCoroutineDispatchers
}
