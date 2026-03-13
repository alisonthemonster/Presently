package journal.gratitude.com.gratitudejournal.logging.wiring

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.logging.PresentlyFirebaseAnalytics
import journal.gratitude.com.gratitudejournal.logging.RealFirebaseAnalytics
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
    fun providesAnalyticsLogger(
        @InternalApi firebase: journal.gratitude.com.gratitudejournal.logging.FirebaseAnalytics,
        crashReporting: CrashReporter
    ): AnalyticsLogger {
        return PresentlyFirebaseAnalytics(firebase, crashReporting)
    }

    @Provides
    @InternalApi
    fun provideFirebaseAnalyticsWrapper(@InternalApi firebase: FirebaseAnalytics): journal.gratitude.com.gratitudejournal.logging.FirebaseAnalytics {
        return RealFirebaseAnalytics(firebase)
    }

    @Provides
    @InternalApi
    fun provideFirebaseAnalytics(@ApplicationContext context: Context): FirebaseAnalytics {
        return FirebaseAnalytics.getInstance(context)
    }

}