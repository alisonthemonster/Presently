package journal.gratitude.com.gratitudejournal.util

import com.crashlytics.android.Crashlytics

interface CrashlyticsWrapper {
    fun logException(throwable: Throwable)

    fun logNonFatal(message: String)
}

class CrashlyticsWrapperImpl: CrashlyticsWrapper {
    override fun logException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    override fun logNonFatal(message: String) {
        Crashlytics.log(message)
    }

}