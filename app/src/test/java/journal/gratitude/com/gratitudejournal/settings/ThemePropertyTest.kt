package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.updateThemeProperty
import journal.gratitude.com.gratitudejournal.settings.model.THEME_PREF
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** The theme user property must report the selected theme at launch and after every change. */
@RunWith(RobolectricTestRunner::class)
class ThemePropertyTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var settings: RealPresentlySettings
    private val analytics = RecordingAnalytics()

    @Before
    fun setUp() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("theme-property-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        settings = RealPresentlySettings(prefs, analytics)
    }

    @Test
    fun `launch reports the default theme when none was ever chosen`() {
        analytics.updateThemeProperty(settings)

        assertThat(analytics.properties).containsEntry("theme", "original")
    }

    @Test
    fun `launch reports a theme chosen before the property was tracked`() {
        prefs.edit().putString(THEME_PREF, "Midnight").commit()

        analytics.updateThemeProperty(settings)

        assertThat(analytics.properties).containsEntry("theme", "Midnight")
    }

    @Test
    fun `choosing a theme updates the property`() {
        settings.setTheme("Midnight")

        assertThat(analytics.properties).containsEntry("theme", "Midnight")
    }

    @Test
    fun `choosing another theme replaces the property`() {
        settings.setTheme("Midnight")
        settings.setTheme("Sunrise")

        assertThat(analytics.properties).containsEntry("theme", "Sunrise")
    }

    private class RecordingAnalytics : AnalyticsLogger {
        val properties = mutableMapOf<String, String>()

        override fun setUserProperty(name: String, value: String) {
            properties[name] = value
        }

        override fun recordEvent(event: String) = Unit
        override fun recordEvent(event: String, details: Map<String, Any>) = Unit
        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
        override fun recordEntryAdded(numEntries: Int) = Unit
        override fun recordView(viewName: String) = Unit
        override fun optOutOfAnalytics() = Unit
        override fun optIntoAnalytics() = Unit
    }
}
