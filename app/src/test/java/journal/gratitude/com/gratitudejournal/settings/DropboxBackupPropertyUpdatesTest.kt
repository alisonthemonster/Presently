package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.settings.model.BACKUP_CADENCE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The dropbox_backup user property must follow every change to the Dropbox token, whichever
 * caller makes it (Settings, or the uploader after a revoked token).
 */
@RunWith(RobolectricTestRunner::class)
class DropboxBackupPropertyUpdatesTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var settings: RealPresentlySettings
    private val analytics = RecordingAnalytics()

    @Before
    fun setUp() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("property-updates-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        settings = RealPresentlySettings(prefs, analytics)
    }

    @Test
    fun `connecting dropbox sets the property to the cadence`() {
        settings.setAccessToken(DbxCredential("token"))

        assertThat(analytics.properties).containsEntry("dropbox_backup", "daily")
    }

    @Test
    fun `connecting reports the stored cadence`() {
        prefs.edit().putString(BACKUP_CADENCE, "1").commit()

        settings.setAccessToken(DbxCredential("token"))

        assertThat(analytics.properties).containsEntry("dropbox_backup", "weekly")
    }

    @Test
    fun `clearing the token sets the property to off`() {
        settings.setAccessToken(DbxCredential("token"))

        settings.clearAccessToken()

        assertThat(analytics.properties).containsEntry("dropbox_backup", "off")
    }

    @Test
    fun `cancelling the auth flow sets the property to off`() {
        settings.setAccessToken(DbxCredential("token"))

        settings.markDropboxAuthAsCancelled()

        assertThat(analytics.properties).containsEntry("dropbox_backup", "off")
    }

    @Test
    fun `starting the auth flow does not change the property`() {
        settings.markDropboxAuthInitiated()

        assertThat(analytics.properties).isEmpty()
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
