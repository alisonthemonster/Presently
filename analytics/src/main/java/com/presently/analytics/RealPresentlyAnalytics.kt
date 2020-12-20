package com.presently.analytics

import ly.count.android.sdk.Countly
import javax.inject.Inject

class CountlyAnalytics @Inject constructor(private val countly: Countly) : PresentlyAnalytics {
    override fun recordEvent(event: String) {
        countly.events().recordEvent(event)
    }

    override fun recordEvent(event: String, details: Map<String, String>) {
        countly.events().recordEvent(event, details)
    }

    override fun recordEvent(event: String, count: Int) {
        countly.events().recordEvent(event, count)
    }

    override fun recordEvent(event: String, count: Int, sum: Double) {
        countly.events().recordEvent(event, count, sum)
    }

    override fun recordView(viewName: String) {
        countly.views().recordView(viewName)
    }
}