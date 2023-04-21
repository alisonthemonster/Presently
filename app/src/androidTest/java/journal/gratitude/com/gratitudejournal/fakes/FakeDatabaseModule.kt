package journal.gratitude.com.gratitudejournal.fakes

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import journal.gratitude.com.gratitudejournal.di.ApplicationModule
import journal.gratitude.com.gratitudejournal.di.DatabaseModule
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.room.EntryDatabase

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object FakeDatabaseModule {

    @Provides
    fun provideEntryDao(database: EntryDatabase): EntryDao {
        return database.entryDao()
    }

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): EntryDatabase {
        return Room.inMemoryDatabaseBuilder(context, EntryDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }
}