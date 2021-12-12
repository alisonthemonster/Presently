package com.presently.logging

interface AnalyticsLogger {
    fun recordEvent(event: String)

    fun recordEvent(event: String, details: Map<String, Any>)

    fun recordSelectEvent(selectedContent: String, selectedContentType: String)

    fun recordEntryAdded(numEntries: Int)

    fun recordView(viewName: String)

}