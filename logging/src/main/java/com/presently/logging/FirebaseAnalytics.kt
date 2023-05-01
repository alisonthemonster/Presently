package com.presently.logging

import android.os.Bundle

interface FirebaseAnalytics {

    fun logEvent(event: String)

    fun logEvent(event: String, bundle: Bundle)

    fun deleteAllAnalyticsDataForUser()

    fun setAnalyticsCollection(enabled: Boolean)
}

class RealFirebaseAnalytics(private val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics) :
    FirebaseAnalytics {
    override fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    override fun logEvent(event: String, bundle: Bundle) {
        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun deleteAllAnalyticsDataForUser() {
        // Clears all analytics data for this app from the device and resets the app instance id.
        firebaseAnalytics.resetAnalyticsData()
    }

    override fun setAnalyticsCollection(enabled: Boolean) {
        // Sets whether analytics collection is enabled for this app on this device.
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled)
    }
}
