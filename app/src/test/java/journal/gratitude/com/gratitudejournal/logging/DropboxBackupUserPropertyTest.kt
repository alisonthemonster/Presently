package journal.gratitude.com.gratitudejournal.logging

import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import org.junit.Test

class DropboxBackupUserPropertyTest {

    @Test
    fun `off ignores the stored cadence`() {
        BackupCadence.values().forEach { cadence ->
            assertThat(dropboxBackupPropertyValue(false, cadence)).isEqualTo("off")
        }
    }

    @Test
    fun `on reports the cadence`() {
        assertThat(dropboxBackupPropertyValue(true, BackupCadence.DAILY)).isEqualTo("daily")
        assertThat(dropboxBackupPropertyValue(true, BackupCadence.WEEKLY)).isEqualTo("weekly")
        assertThat(dropboxBackupPropertyValue(true, BackupCadence.EVERY_CHANGE))
            .isEqualTo("every_change")
    }

    @Test
    fun `values fit the 36 character user property limit`() {
        BackupCadence.values().forEach { cadence ->
            listOf(true, false).forEach { on ->
                assertThat(dropboxBackupPropertyValue(on, cadence).length).isAtMost(36)
            }
        }
        assertThat(DROPBOX_BACKUP_PROPERTY.length).isAtMost(24)
    }

    @Test
    fun `a connected user is reported with their cadence`() {
        val settings = mock<PresentlySettings> {
            on { getAccessToken() } doReturn DbxCredential("token")
            on { getAutomaticBackupCadence() } doReturn BackupCadence.WEEKLY
        }
        val analytics = RecordingLogger()

        analytics.updateDropboxBackupProperty(settings)

        assertThat(analytics.properties).containsExactly("dropbox_backup", "weekly")
    }

    @Test
    fun `a user without a token is reported as off`() {
        val settings = mock<PresentlySettings> {
            on { getAccessToken() } doReturn null
            on { getAutomaticBackupCadence() } doReturn BackupCadence.DAILY
        }
        val analytics = RecordingLogger()

        analytics.updateDropboxBackupProperty(settings)

        assertThat(analytics.properties).containsExactly("dropbox_backup", "off")
    }

    private class RecordingLogger : AnalyticsLogger {
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
