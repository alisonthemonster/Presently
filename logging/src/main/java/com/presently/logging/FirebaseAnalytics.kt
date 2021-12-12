package com.presently.logging

import android.os.Bundle

interface FirebaseAnalytics {

    fun logEvent(event: String)

    fun logEvent(event: String, bundle: Bundle)

}

class RealFirebaseAnalytics(private val firebaseAnalytics: com.google.firebase.analytics.FirebaseAnalytics) :
    FirebaseAnalytics {
    override fun logEvent(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    override fun logEvent(event: String, bundle: Bundle) {
        firebaseAnalytics.logEvent(event, bundle)
    }
}