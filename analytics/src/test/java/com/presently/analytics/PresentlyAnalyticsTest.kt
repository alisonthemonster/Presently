package com.presently.analytics

import android.os.Build
import android.os.Bundle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class PresentlyAnalyticsTest {
    var eventWasRecored = false
    var eventWasRecordedWithDetails = false
    var detailsFake: Map<String, Any> = mutableMapOf()
    var recordViewWasCalled = false
    var consentWasRemoved = false

    val countly = object : CountlyAnalytics {
        override fun recordEvent(event: String) {
            eventWasRecored = true
        }

        override fun recordEvent(event: String, details: Map<String, Any>) {
            eventWasRecordedWithDetails = true
            detailsFake = details
        }

        override fun recordView(viewName: String) {
            recordViewWasCalled = true
        }

        override fun removeConsentAll() {
            consentWasRemoved = true
        }
    }

    var firebaseEventLogged = false
    var firebaseEventLoggedWithDetails = false
    var bundleFake: Bundle? = null


    val firebase = object : com.presently.analytics.FirebaseAnalytics {
        override fun logEvent(event: String) {
            firebaseEventLogged = true
        }

        override fun logEvent(event: String, bundle: Bundle) {
            firebaseEventLoggedWithDetails = true
            bundleFake = bundle
        }

    }

    @Test
    fun analyticsRecordEventWithName() {
        val eventName = "test"

        val analytics = RealPresentlyAnalytics(countly, firebase)

        analytics.recordEvent(eventName)
        //assert(eventWasRecored)
        assert(firebaseEventLogged)
    }

    @Test
    fun analyticsRecordEventWithNameAndDetails() {
        val eventName = "test"
        val details = mapOf("hello" to "hiya", "hey" to 2)

        val analytics = RealPresentlyAnalytics(countly, firebase)

        analytics.recordEvent(eventName, details)

        val expectedBundle = Bundle()
        expectedBundle.putString("hello", "hiya")
        expectedBundle.putInt("hey", 2)
//        assert(eventWasRecordedWithDetails)
//        assert(details == detailsFake)
        assert(firebaseEventLoggedWithDetails)
        assert(bundleFake?.get("hello") == "hiya" && bundleFake?.get("hey") == 2)
    }

//    @Test
//    fun analyticsRecordView() {
//        val analytics = RealPresentlyAnalytics(countly, firebase)
//
//        val name = "Screen Name!"
//        analytics.recordView(name)
//        assert(recordViewWasCalled)
//    }
//
//    @Test
//    fun analyticsOptOut() {
//        val analytics = RealPresentlyAnalytics(countly, firebase)
//
//        analytics.optOutOfAnalytics()
//        assert(consentWasRemoved)
//    }
}