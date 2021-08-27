package com.presently.presently_local_source.wiring

import android.content.Context
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.RealPresentlyLocalSource
import com.presently.presently_local_source.database.EntryDao
import com.presently.presently_local_source.database.EntryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentlyLocalSourceModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): EntryDatabase {
        return EntryDatabase.getDatabase(context)
    }

    @Provides
    fun provideEntryDao(
        database: EntryDatabase
    ): EntryDao {
        return database.entryDao()
    }

    @Singleton
    @Provides
    fun provideLocalSource(localSource: RealPresentlyLocalSource): PresentlyLocalSource {
        return localSource
    }
}