package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupPreferencesTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(context)
    }
    private val analytics = RecordingAnalyticsLogger()
    private val backupPreferences by lazy {
        BackupPreferences(sharedPreferences, analytics)
    }

    @Before
    fun setUp() {
        sharedPreferences.edit().clear().commit()
        analytics.events.clear()
    }

    @Test
    fun setDropboxCredential_updatesDropboxState() {
        backupPreferences.setDropboxCredential(DbxCredential("token"), "dropbox@example.com")

        val state = backupPreferences.getDropboxState()

        assertThat(state.isConnected).isTrue()
        assertThat(state.accountEmail).isEqualTo("dropbox@example.com")
        assertThat(backupPreferences.getDropboxCredential()?.accessToken).isEqualTo("token")
        assertThat(analytics.events).contains("dropboxAuthorizaitonSuccess")
    }

    @Test
    fun updateFrequency_persistsProviderSpecificFrequency() {
        backupPreferences.updateFrequency(BackupProvider.DROPBOX, BackupFrequency.MONTHLY)
        backupPreferences.updateFrequency(BackupProvider.GOOGLE_DRIVE, BackupFrequency.ON_EVERY_CHANGE)

        assertThat(backupPreferences.getDropboxState().frequency).isEqualTo(BackupFrequency.MONTHLY)
        assertThat(backupPreferences.getGoogleDriveState().frequency).isEqualTo(BackupFrequency.ON_EVERY_CHANGE)
    }

    @Test
    fun markDropboxAuthInitiated_tracksAttemptStateUntilCancelled() {
        backupPreferences.markDropboxAuthInitiated()

        assertThat(backupPreferences.wasDropboxAuthInitiated()).isTrue()

        backupPreferences.markDropboxAuthCancelled()

        assertThat(backupPreferences.wasDropboxAuthInitiated()).isFalse()
        assertThat(backupPreferences.getDropboxState().isConnected).isFalse()
        assertThat(analytics.events).contains("dropboxAuthorizaitonQuit")
    }

    @Test
    fun setGoogleDriveConnection_updatesGoogleDriveState() {
        backupPreferences.setGoogleDriveConnection("drive@example.com")
        backupPreferences.updateLastBackupTimestamp(BackupProvider.GOOGLE_DRIVE, 1234L)

        val state = backupPreferences.getGoogleDriveState()

        assertThat(state.isConnected).isTrue()
        assertThat(state.accountEmail).isEqualTo("drive@example.com")
        assertThat(state.lastBackupTimestamp).isEqualTo(1234L)
    }
}

private class RecordingAnalyticsLogger : AnalyticsLogger {
    val events = mutableListOf<String>()

    override fun recordEvent(event: String) {
        events += event
    }

    override fun recordEvent(event: String, details: Map<String, Any>) {
        events += event
    }

    override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit

    override fun recordEntryAdded(numEntries: Int) = Unit

    override fun recordView(viewName: String) = Unit

    override fun optOutOfAnalytics() = Unit

    override fun optIntoAnalytics() = Unit
}
