package journal.gratitude.com.gratitudejournal.testUtils

import java.util.Locale
import org.junit.rules.TestWatcher
import org.junit.runner.Description

class LocaleTestRule(
    private val locale: Locale
) : TestWatcher() {

    private lateinit var previousLocale: Locale

    override fun starting(description: Description) {
        previousLocale = Locale.getDefault()
        Locale.setDefault(locale)
    }

    override fun finished(description: Description) {
        Locale.setDefault(previousLocale)
    }
}
