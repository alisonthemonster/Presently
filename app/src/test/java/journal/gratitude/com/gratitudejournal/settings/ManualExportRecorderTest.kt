package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.EXPORTED_DATA
import journal.gratitude.com.gratitudejournal.model.EXPORTING_BACKUP_ERROR
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ManualExportRecorderTest {

    private lateinit var store: RealBackupStatusStore
    private val analytics = RecordingAnalytics()
    private lateinit var recorder: ManualExportRecorder

    @Before
    fun setUp() {
        val prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("export-recorder-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = RealBackupStatusStore(prefs) { 4_200L }
        recorder = ManualExportRecorder(store, analytics)
    }

    @Test
    fun `a successful export stores the time and logs where it started`() {
        recorder.onExported()

        assertThat(store.getLastManualExportAt()).isEqualTo(4_200L)
        assertThat(analytics.events).containsExactly(EXPORTED_DATA to mapOf("source" to "settings"))
    }

    @Test
    fun `the source can be something other than settings`() {
        recorder.onExported(source = "banner")

        assertThat(analytics.events).containsExactly(EXPORTED_DATA to mapOf("source" to "banner"))
    }

    @Test
    fun `a failed export logs the failure and does not count as a backup`() {
        recorder.onExportFailed()

        assertThat(store.getLastManualExportAt()).isNull()
        assertThat(analytics.events)
            .containsExactly(EXPORTING_BACKUP_ERROR to mapOf("source" to "settings"))
    }

    @Test
    fun `event values are strings so analytics does not drop them`() {
        recorder.onExported()
        recorder.onExportFailed()

        assertThat(analytics.events.flatMap { it.second.values }.all { it is String }).isTrue()
    }

    private class RecordingAnalytics : AnalyticsLogger {
        val events = mutableListOf<Pair<String, Map<String, Any>>>()

        override fun recordEvent(event: String) {
            events += event to emptyMap()
        }

        override fun recordEvent(event: String, details: Map<String, Any>) {
            events += event to details
        }

        override fun setUserProperty(name: String, value: String) = Unit
        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
        override fun recordEntryAdded(numEntries: Int) = Unit
        override fun recordView(viewName: String) = Unit
        override fun optOutOfAnalytics() = Unit
        override fun optIntoAnalytics() = Unit
    }
}
