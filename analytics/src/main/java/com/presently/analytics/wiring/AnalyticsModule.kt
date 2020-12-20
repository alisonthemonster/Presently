package com.presently.analytics.wiring

import android.app.Application
import com.presently.analytics.CountlyAnalytics
import com.presently.analytics.PresentlyAnalytics
import dagger.Binds
import dagger.Module
import dagger.Provides
import ly.count.android.sdk.Countly
import ly.count.android.sdk.CountlyConfig
import javax.inject.Singleton
import com.presently.analytics.BuildConfig


@Module(includes = [AnalyticsModuleBinds::class])
object AnalyticsModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideCountly(application: Application): Countly {
        val appKey = BuildConfig.COUNTLY_APP_KEY
        val serverUrl = "http://104.236.20.250"
        val config = CountlyConfig(application, appKey, serverUrl)
        config.setLoggingEnabled(true)
        return Countly.sharedInstance()
            .init(config)
    }
}

@Module
abstract class AnalyticsModuleBinds {

    @Singleton
    @Binds
    abstract fun bindAnalytics(repo: CountlyAnalytics): PresentlyAnalytics
}