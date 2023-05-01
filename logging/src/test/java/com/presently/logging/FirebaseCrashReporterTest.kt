package com.presently.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Test
import java.lang.IllegalArgumentException

class FirebaseCrashReporterTest {

    @Test
    fun `GIVEN a FirebaseCrashReporter WHEN logHandledException THEN firebase recordException is called`() {
        val firebase = mock<FirebaseCrashlytics>()
        val firebaseCrashReporter = FirebaseCrashReporter(firebase)
        val expected = IllegalArgumentException("bad bad times")
        firebaseCrashReporter.logHandledException(expected)

        verify(firebase).recordException(expected)
    }

    @Test
    fun `GIVEN a FirebaseCrashReporter WHEN optOutOfCrashReporting THEN setCrashlyticsCollectionEnabled recordException is called`() {
        val firebase = mock<FirebaseCrashlytics>()
        val firebaseCrashReporter = FirebaseCrashReporter(firebase)
        firebaseCrashReporter.optOutOfCrashReporting()

        verify(firebase).setCrashlyticsCollectionEnabled(false)
    }

    @Test
    fun `GIVEN a FirebaseCrashReporter WHEN optIntoCrashReporting THEN setCrashlyticsCollectionEnabled recordException is called`() {
        val firebase = mock<FirebaseCrashlytics>()
        val firebaseCrashReporter = FirebaseCrashReporter(firebase)
        firebaseCrashReporter.optIntoCrashReporting()

        verify(firebase).setCrashlyticsCollectionEnabled(true)
    }
}
