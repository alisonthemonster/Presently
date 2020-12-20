package com.presently.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import ly.count.android.sdk.Countly
import javax.inject.Inject

class RealPresentlyAnalytics @Inject constructor(private val countly: Countly, private val firebase: FirebaseAnalytics) : PresentlyAnalytics {
    override fun recordEvent(event: String) {
        countly.events().recordEvent(event)
        firebase.logEvent(event, null)
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        countly.events().recordEvent(event, details)
        val bundle = Bundle()
        firebase.logEvent(event, bundle)
    }

    override fun recordEvent(event: String, count: Int) {
        countly.events().recordEvent(event, count)
        firebase.logEvent(event, null)
    }

    override fun recordEvent(event: String, count: Int, sum: Double) {
        countly.events().recordEvent(event, count, sum)
        firebase.logEvent(event, null)
    }

    override fun recordView(viewName: String) {
        countly.views().recordView(viewName)
    }

}