package com.presently.logging

import android.os.Bundle

internal class PresentlyFirebaseAnalytics(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val crashReporter: CrashReporter
) :
    AnalyticsLogger {

    override fun recordEvent(event: String) {
        firebaseAnalytics.logEvent(event)
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
        bundle.putString(
            com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME,
            selectedContent
        )
        bundle.putString(
            com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_ID,
            selectedContent
        )
        bundle.putString(
            com.google.firebase.analytics.FirebaseAnalytics.Param.CONTENT_TYPE,
            selectedContentType
        )
        firebaseAnalytics.logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.SELECT_CONTENT,
            bundle
        )
    }

    override fun recordEntryAdded(numEntries: Int) {
        val bundle = Bundle()
        bundle.putInt(com.google.firebase.analytics.FirebaseAnalytics.Param.LEVEL, numEntries)

        firebaseAnalytics.logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.LEVEL_UP,
            bundle
        )
    }

    override fun recordView(viewName: String) {
        val bundle = Bundle()
        bundle.putString(
            com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_NAME,
            viewName
        )
        bundle.putString(
            com.google.firebase.analytics.FirebaseAnalytics.Param.SCREEN_CLASS,
            viewName
        )
        firebaseAnalytics.logEvent(
            com.google.firebase.analytics.FirebaseAnalytics.Event.SCREEN_VIEW,
            bundle
        )
    }

    override fun optOutOfAnalytics() {
        firebaseAnalytics.deleteAllAnalyticsDataForUser()
        firebaseAnalytics.setAnalyticsCollection(enabled = false)
        crashReporter.optOutOfCrashReporting()
    }

    override fun optIntoAnalytics() {
        firebaseAnalytics.setAnalyticsCollection(enabled = true)
        crashReporter.optIntoCrashReporting()
    }
}