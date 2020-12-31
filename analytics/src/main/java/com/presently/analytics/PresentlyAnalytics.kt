package com.presently.analytics

interface PresentlyAnalytics {

    fun recordEvent(event: String)

    fun recordEvent(event: String, details: Map<String, Any>)

    fun recordView(viewName: String)

    fun optOutOfAnalytics()

}

