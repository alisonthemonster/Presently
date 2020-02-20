package journal.gratitude.com.gratitudejournal.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import javax.inject.Singleton


@Module(includes = [ApplicationModuleBinds::class])
object ApplicationModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideEntryDao(
        database: EntryDatabase
    ): EntryDao {
        return database.entryDao()
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideDatabase(context: Context): EntryDatabase {
        return EntryDatabase.getDatabase(context)
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideSharedPreferences(context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

}

@Module
abstract class ApplicationModuleBinds {

    @Singleton
    @Binds
    abstract fun bindRepository(repo: EntryRepositoryImpl): EntryRepository
}