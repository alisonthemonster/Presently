package com.presently.logging.wiring

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presently.logging.CrashReporter
import com.presently.logging.FirebaseCrashReporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CrashReportingModule {

    @Provides
    fun providesCrashReporter(firebase: FirebaseCrashlytics): CrashReporter {
        return FirebaseCrashReporter(firebase)
    }

    @Provides
    fun provideFirebaseCrashlytics(): FirebaseCrashlytics {
        return FirebaseCrashlytics.getInstance()
    }
}
