package com.presently.logging

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics


internal class PresentlyFirebaseAnalytics(private val firebaseAnalytics: FirebaseAnalytics) :
    AnalyticsLogger {

    override fun recordEvent(event: String) {
        firebaseAnalytics.logEvent(event, null)
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        val bundle = Bundle()
        for (item in details) {
            when (item.value) {
                is String -> bundle.putString(item.key, item.value as String)
                is Int -> bundle.putInt(item.key, item.value as Int)
            }
        }
        firebaseAnalytics.logEvent(event, bundle)
    }

    override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, selectedContent)
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, selectedContent)
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, selectedContentType)
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    override fun recordEntryAdded(numEntries: Int) {
        val bundle = Bundle()
        bundle.putInt(FirebaseAnalytics.Param.LEVEL, numEntries)

        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
    }

    override fun recordView(viewName: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.SCREEN_NAME, viewName)
        bundle.putString(FirebaseAnalytics.Param.SCREEN_CLASS, "MainActivity")
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}