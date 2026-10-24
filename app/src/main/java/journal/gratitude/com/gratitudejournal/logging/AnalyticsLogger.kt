package journal.gratitude.com.gratitudejournal.logging

interface AnalyticsLogger {
    fun recordEvent(event: String)

    fun recordEvent(event: String, details: Map<String, Any>)

    fun recordSelectEvent(selectedContent: String, selectedContentType: String)

    fun recordEntryAdded(numEntries: Int)

    fun recordView(viewName: String)

    fun optOutOfAnalytics()

    fun optIntoAnalytics()

    // Has a default so the many test fakes of this interface don't each need to implement it.
    fun setUserProperty(name: String, value: String) {}
}