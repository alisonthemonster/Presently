package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import javax.inject.Singleton


@Module(includes = [ApplicationModuleBinds::class])
@InstallIn(SingletonComponent::class)
object ApplicationModule {

    @Provides
    fun provideEntryDao(
        database: EntryDatabase
    ): EntryDao {
        return database.entryDao()
    }

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): EntryDatabase {
        return EntryDatabase.getDatabase(context)
    }

}

@Module
@InstallIn(SingletonComponent::class)
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: EntryRepositoryImpl): EntryRepository
}