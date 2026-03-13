package journal.gratitude.com.gratitudejournal.logging

import java.lang.Exception

interface CrashReporter {

    fun logHandledException(exception: Exception)

    fun optOutOfCrashReporting()

    fun optIntoCrashReporting()
}