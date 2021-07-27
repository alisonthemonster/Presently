package com.presently.logging.wiring

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.presently.logging.AnalyticsLogger
import com.presently.logging.PresentlyFirebaseAnalytics
import com.presently.logging.RealFirebaseAnalytics
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Retention(AnnotationRetention.BINARY)
@Qualifier
private annotation class InternalApi

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    fun providesAnalyticsLogger(firebase: com.presently.logging.FirebaseAnalytics): AnalyticsLogger {
        return PresentlyFirebaseAnalytics(firebase)
    }

    @Provides
    fun provideFirebaseAnalyticsWrapper(firebase: FirebaseAnalytics): com.presently.logging.FirebaseAnalytics {
        return RealFirebaseAnalytics(firebase)
    }

    @Provides
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

}