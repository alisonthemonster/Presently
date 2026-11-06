package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class LastBackupAgePropertyTest {

    private val now = TimeUnit.DAYS.toMillis(1000)
    private fun daysAgo(d: Long, extraMillis: Long = 0) = now - TimeUnit.DAYS.toMillis(d) - extraMillis

    private var storeClock = 0L
    private lateinit var store: RealBackupStatusStore
    private val analytics = RecordingAnalytics()

    @Before
    fun setUp() {
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("last-backup-age-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = RealBackupStatusStore(prefs) { storeClock }
    }

    private fun settings(connected: Boolean): PresentlySettings = mock {
        on { getAccessToken() } doReturn (if (connected) DbxCredential("token") else null)
    }

    @Test
    fun `nothing recorded is unknown`() {
        assertThat(lastBackupAgeValue(null, now)).isEqualTo("unknown")
    }

    @Test
    fun `buckets by whole days elapsed`() {
        assertThat(lastBackupAgeValue(now, now)).isEqualTo("today")
        assertThat(lastBackupAgeValue(daysAgo(1) + 1, now)).isEqualTo("today") // just under a day
        assertThat(lastBackupAgeValue(daysAgo(1), now)).isEqualTo("1_7_days")
        assertThat(lastBackupAgeValue(daysAgo(7), now)).isEqualTo("1_7_days")
        assertThat(lastBackupAgeValue(daysAgo(8), now)).isEqualTo("8_30_days")
        assertThat(lastBackupAgeValue(daysAgo(30), now)).isEqualTo("8_30_days")
        assertThat(lastBackupAgeValue(daysAgo(31), now)).isEqualTo("over_30_days")
        assertThat(lastBackupAgeValue(daysAgo(900), now)).isEqualTo("over_30_days")
    }

    @Test
    fun `a backup time in the future counts as today`() {
        assertThat(lastBackupAgeValue(now + TimeUnit.DAYS.toMillis(3), now)).isEqualTo("today")
    }

    @Test
    fun `values fit the user property limits`() {
        listOf(null, now, daysAgo(3), daysAgo(20), daysAgo(90)).forEach {
            assertThat(lastBackupAgeValue(it, now).length).isAtMost(36)
        }
        assertThat(LAST_BACKUP_AGE_PROPERTY.length).isAtMost(24)
    }

    @Test
    fun `a user with no recorded backup is reported as unknown`() {
        analytics.updateLastBackupAgeProperty(settings(connected = false), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "unknown")
    }

    @Test
    fun `a recent dropbox backup is reported`() {
        storeClock = daysAgo(3)
        store.recordDropboxUploadSuccess()

        analytics.updateLastBackupAgeProperty(settings(connected = false), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "1_7_days")
    }

    @Test
    fun `a manual export counts`() {
        storeClock = daysAgo(20)
        store.recordManualExport()

        analytics.updateLastBackupAgeProperty(settings(connected = false), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "8_30_days")
    }

    @Test
    fun `the most recent of the two backups is used`() {
        storeClock = daysAgo(60)
        store.recordManualExport()
        storeClock = daysAgo(2)
        store.recordDropboxUploadSuccess()

        analytics.updateLastBackupAgeProperty(settings(connected = false), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "1_7_days")
    }

    @Test
    fun `a connected user is judged on dropbox alone`() {
        storeClock = daysAgo(60)
        store.recordDropboxUploadSuccess()
        storeClock = daysAgo(1)
        store.recordManualExport()

        analytics.updateLastBackupAgeProperty(settings(connected = true), store, now)

        // the recent manual export must not hide a sync that has stopped
        assertThat(analytics.properties).containsExactly("last_backup_age", "over_30_days")
    }

    @Test
    fun `a connected user with only a manual export has no dropbox backup on record`() {
        storeClock = daysAgo(2)
        store.recordManualExport()

        analytics.updateLastBackupAgeProperty(settings(connected = true), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "unknown")
    }

    @Test
    fun `a connected user with a recent dropbox backup is reported`() {
        storeClock = daysAgo(0, extraMillis = 3_600_000)
        store.recordDropboxUploadSuccess()

        analytics.updateLastBackupAgeProperty(settings(connected = true), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "today")
    }

    @Test
    fun `someone who turned dropbox off is judged on their latest backup of either kind`() {
        storeClock = daysAgo(90)
        store.recordDropboxUploadSuccess()
        storeClock = daysAgo(5)
        store.recordManualExport()

        analytics.updateLastBackupAgeProperty(settings(connected = false), store, now)

        assertThat(analytics.properties).containsExactly("last_backup_age", "1_7_days")
    }

    @Test
    fun `which backup time is used`() {
        assertThat(lastBackupAtMillis(true, 10L, 99L)).isEqualTo(10L)
        assertThat(lastBackupAtMillis(true, null, 99L)).isNull()
        assertThat(lastBackupAtMillis(false, 10L, 99L)).isEqualTo(99L)
        assertThat(lastBackupAtMillis(false, 50L, 20L)).isEqualTo(50L)
        assertThat(lastBackupAtMillis(false, null, null)).isNull()
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
