package com.presently.analytics

interface PresentlyAnalytics {

    fun recordEvent(event: String)

    fun recordEvent(event: String, details: Map<String, String>)

    fun recordEvent(event: String, count: Int)

    fun recordEvent(event: String, count: Int, sum: Double)

    fun recordView(viewName: String)

}

