package journal.gratitude.com.gratitudejournal.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.room.EntryDao

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    val entryDao: EntryDao
}
