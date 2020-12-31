package com.presently.analytics.wiring

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.presently.analytics.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig
import javax.inject.Singleton


@Module(includes = [AnalyticsModuleBinds::class])
object AnalyticsModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideCountly(application: Application): CountlyAnalytics {
        val appKey = BuildConfig.COUNTLY_APP_KEY
        val serverUrl = "http://104.236.20.250"
        val config = CountlyConfig(application, appKey, serverUrl)
        config.setLoggingEnabled(true)
        return RealCountlyAnalytics(Countly.sharedInstance()
            .init(config))
    }

    @JvmStatic
    @Singleton
    @Provides
    fun provideFirebase(application: Application): com.presently.analytics.FirebaseAnalytics {
        return RealFirebaseAnalytics(FirebaseAnalytics.getInstance(application))
    }

}

@Module
abstract class AnalyticsModuleBinds {

    @Singleton
    @Binds
    abstract fun bindAnalytics(repo: RealPresentlyAnalytics): PresentlyAnalytics
}