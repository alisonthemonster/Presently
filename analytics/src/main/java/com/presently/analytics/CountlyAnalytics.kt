package com.presently.analytics

import ly.count.android.sdk.Countly

interface CountlyAnalytics {
    fun recordEvent(event: String)

    fun recordEvent(event: String, details: Map<String, Any>)

    fun recordView(viewName: String)

    fun removeConsentAll()
}

class RealCountlyAnalytics (private val countly: Countly): CountlyAnalytics {
    override fun recordEvent(event: String) {
        countly.events().recordEvent(event)
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        countly.events().recordEvent(event, details)
    }

    override fun recordView(viewName: String) {
        countly.views().recordView(viewName)
    }

    override fun removeConsentAll() {
        countly.consent().removeConsentAll()
    }

}