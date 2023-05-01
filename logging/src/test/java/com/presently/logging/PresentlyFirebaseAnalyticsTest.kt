package com.presently.logging

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.lang.Exception

@RunWith(RobolectricTestRunner::class)
class PresentlyFirebaseAnalyticsTest {

    private val crashReporter = object : CrashReporter {
        override fun logHandledException(exception: Exception) {}
        override fun optOutOfCrashReporting() {}
        override fun optIntoCrashReporting() {}
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN recordEvent called THEN firebase is called`() {
        var eventName = ""
        var logEventWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                logEventWasCalled = true
                eventName = event
            }

            override fun logEvent(event: String, bundle: Bundle) {
                fail("bundle logEvent shouldn't be called")
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("bundle deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                fail("bundle setAnalyticsCollection shouldn't be called")
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        val expected = "eventName"
        presentlyAnalytics.recordEvent(expected)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo(expected)
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN recordEvent called with details THEN firebase is called`() {
        var eventName = ""
        var bundle: Bundle? = null
        var logEventWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called without a bundle")
            }

            override fun logEvent(event: String, b: Bundle) {
                logEventWasCalled = true
                eventName = event
                bundle = b
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("bundle deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                fail("bundle setAnalyticsCollection shouldn't be called")
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        val expected = "eventName"
        val given = mapOf("a" to "apple", "b" to 11)
        val expectedBundle = bundleOf("a" to "apple", "b" to 11)
        presentlyAnalytics.recordEvent(expected, given)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo(expected)
        assertThat(bundle.toString()).isEqualTo(expectedBundle.toString())
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN recordSelectEvent is called THEN firebase is called`() {
        var eventName = ""
        var bundle: Bundle? = null
        var logEventWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called without a bundle")
            }

            override fun logEvent(event: String, b: Bundle) {
                logEventWasCalled = true
                eventName = event
                bundle = b
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("bundle deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                fail("bundle setAnalyticsCollection shouldn't be called")
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        val expected = "eventName"
        val expectedType = "eventType"
        val expectedBundle = bundleOf(
            "item_name" to expected,
            "item_id" to expected,
            "content_type" to expectedType
        )
        presentlyAnalytics.recordSelectEvent(expected, expectedType)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo("select_content")
        assertThat(bundle.toString()).isEqualTo(expectedBundle.toString())
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN recordEntryAdded is called THEN firebase is called`() {
        var eventName = ""
        var bundle: Bundle? = null
        var logEventWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called without a bundle")
            }

            override fun logEvent(event: String, b: Bundle) {
                logEventWasCalled = true
                eventName = event
                bundle = b
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("bundle deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                fail("bundle setAnalyticsCollection shouldn't be called")
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        val expected = 100
        val expectedBundle = bundleOf(
            "level" to expected
        )
        presentlyAnalytics.recordEntryAdded(expected)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo("level_up")
        assertThat(bundle.toString()).isEqualTo(expectedBundle.toString())
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN recordView is called THEN firebase is called`() {
        var eventName = "screenName"
        var bundle: Bundle? = null
        var logEventWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called without a bundle")
            }

            override fun logEvent(event: String, b: Bundle) {
                logEventWasCalled = true
                eventName = event
                bundle = b
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("bundle deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                fail("bundle setAnalyticsCollection shouldn't be called")
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        val expected = "MyFragment"
        val expectedBundle = bundleOf(
            "screen_name" to expected,
            "screen_class" to expected
        )
        presentlyAnalytics.recordView(expected)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo("screen_view")
        assertThat(bundle.toString()).isEqualTo(expectedBundle.toString())
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN optOutOfAnalytics called THEN firebase is called`() {
        var deleteAllWasCalled = false
        var setAnalyticsCollectionWasCalled = false
        var optOutOfCrashReportingWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called")
            }

            override fun logEvent(event: String, bundle: Bundle) {
                fail("bundle logEvent shouldn't be called")
            }

            override fun deleteAllAnalyticsDataForUser() {
                deleteAllWasCalled = true
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                if (!enabled) {
                    setAnalyticsCollectionWasCalled = true
                }
            }
        }

        val crashReporter = object : CrashReporter {
            override fun logHandledException(exception: Exception) {}
            override fun optOutOfCrashReporting() {
                optOutOfCrashReportingWasCalled = true
            }
            override fun optIntoCrashReporting() {}
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        presentlyAnalytics.optOutOfAnalytics()

        assertThat(optOutOfCrashReportingWasCalled).isTrue()
        assertThat(deleteAllWasCalled).isTrue()
        assertThat(setAnalyticsCollectionWasCalled).isTrue()
    }

    @Test
    fun `GIVEN PresentlyFirebaseAnalytics WHEN optIntoAnalytics called THEN firebase is called`() {
        var setAnalyticsCollectionWasCalled = false
        var optIntoCrashReportingWasCalled = false
        val firebaseAnalytics = object : FirebaseAnalytics {
            override fun logEvent(event: String) {
                fail("logEvent shouldn't be called")
            }

            override fun logEvent(event: String, bundle: Bundle) {
                fail("bundle logEvent shouldn't be called")
            }

            override fun deleteAllAnalyticsDataForUser() {
                fail("deleteAllAnalyticsDataForUser shouldn't be called")
            }

            override fun setAnalyticsCollection(enabled: Boolean) {
                if (enabled) {
                    setAnalyticsCollectionWasCalled = true
                }
            }
        }

        val crashReporter = object : CrashReporter {
            override fun logHandledException(exception: Exception) {}
            override fun optOutOfCrashReporting() {}
            override fun optIntoCrashReporting() {
                optIntoCrashReportingWasCalled = true
            }
        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics, crashReporter)

        presentlyAnalytics.optIntoAnalytics()

        assertThat(optIntoCrashReportingWasCalled).isTrue()
        assertThat(setAnalyticsCollectionWasCalled).isTrue()
    }
}
