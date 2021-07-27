package com.presently.logging

import android.os.Bundle
import androidx.core.os.bundleOf
import com.google.common.truth.Truth.assertThat
import com.google.firebase.analytics.FirebaseAnalytics.Param.ITEM_NAME
import junit.framework.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PresentlyFirebaseAnalyticsTest {

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

        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics)

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

        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics)

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

        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics)

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

        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics)

        val expected = 100
        val expectedBundle = bundleOf(
            "level" to expected,
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

        }
        val presentlyAnalytics = PresentlyFirebaseAnalytics(firebaseAnalytics)

        val expected = "MyFragment"
        val expectedBundle = bundleOf(
            "screen_name" to expected,
            "screen_class" to expected,
        )
        presentlyAnalytics.recordView(expected)

        assertThat(logEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo("screen_view")
        assertThat(bundle.toString()).isEqualTo(expectedBundle.toString())
    }
}