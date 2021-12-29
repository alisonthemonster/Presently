package com.presently.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.lang.Exception
import javax.inject.Inject

internal class FirebaseCrashReporter @Inject constructor(private val firebase: FirebaseCrashlytics): CrashReporter {

    override fun logHandledException(exception: Exception) {
        firebase.recordException(exception)
    }

    override fun optOutOfCrashReporting() {
        //The override value persists across launches of Presently
        firebase.setCrashlyticsCollectionEnabled(false)
    }

    override fun optIntoCrashReporting() {
        //The override value persists across launches of Presently
        firebase.setCrashlyticsCollectionEnabled(true)
    }
}