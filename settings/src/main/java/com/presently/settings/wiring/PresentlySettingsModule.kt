package com.presently.settings.wiring

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.presently.settings.PresentlySettings
import com.presently.settings.RealPresentlySettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object PresentlySettingsModule {

    @Provides
    fun providesPresentlySettings(realPresentlySettings: RealPresentlySettings): PresentlySettings {
        return realPresentlySettings
    }

    @Provides
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
}
