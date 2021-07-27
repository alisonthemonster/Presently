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
}