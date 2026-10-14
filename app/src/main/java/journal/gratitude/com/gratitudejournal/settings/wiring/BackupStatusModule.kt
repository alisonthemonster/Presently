package journal.gratitude.com.gratitudejournal.settings.wiring

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.settings.BackupStatusStore
import journal.gratitude.com.gratitudejournal.settings.RealBackupStatusStore
import javax.inject.Singleton

/**
 * Kept separate from [PresentlySettingsModule] so instrumented tests that replace that module
 * still get a real store.
 */
@Module
@InstallIn(SingletonComponent::class)
object BackupStatusModule {

    @Provides
    @Singleton
    fun provideBackupStatusStore(@ApplicationContext context: Context): BackupStatusStore =
        RealBackupStatusStore(PreferenceManager.getDefaultSharedPreferences(context))
}
