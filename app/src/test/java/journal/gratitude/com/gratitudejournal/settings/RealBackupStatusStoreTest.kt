package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailure
import journal.gratitude.com.gratitudejournal.util.backups.status.UploadFailureType
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RealBackupStatusStoreTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var store: RealBackupStatusStore
    private var nowMillis = 100L

    @Before
    fun setUp() {
        prefs = ApplicationProvider.getApplicationContext<Context>()
            .getSharedPreferences("backup-status-test", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        store = RealBackupStatusStore(prefs) { nowMillis }
    }

    @Test
    fun `fresh store has no recorded state`() {
        assertThat(store.getLastDropboxSuccessAt()).isNull()
        assertThat(store.getLastManualExportAt()).isNull()
        assertThat(store.getLastUploadFailure()).isNull()
        assertThat(store.getDropboxEnabledAt()).isNull()
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isFalse()
    }

    @Test
    fun `success is stamped with the current time and clears a failure`() {
        store.recordDropboxUploadFailure(UploadFailureType.OTHER)
        nowMillis = 200L
        store.recordDropboxUploadSuccess()

        assertThat(store.getLastDropboxSuccessAt()).isEqualTo(200L)
        assertThat(store.getLastUploadFailure()).isNull()
    }

    @Test
    fun `failure round trips`() {
        store.recordDropboxUploadFailure(UploadFailureType.STORAGE_FULL)
        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.STORAGE_FULL, 100L))
    }

    @Test
    fun `every failure type round trips through its persisted code`() {
        UploadFailureType.entries.forEach { type ->
            store.recordDropboxUploadSuccess()
            store.recordDropboxUploadFailure(type)
            assertThat(store.getLastUploadFailure()?.type).isEqualTo(type)
        }
    }

    @Test
    fun `persisted failure codes are stable`() {
        assertThat(UploadFailureType.entries.map { it.code }).containsExactly(
            "auth", "storage_full", "network", "transient_server", "other"
        )
    }

    @Test
    fun `transient failure does not overwrite an unresolved actionable failure`() {
        store.recordDropboxUploadFailure(UploadFailureType.STORAGE_FULL)
        nowMillis = 200L
        store.recordDropboxUploadFailure(UploadFailureType.NETWORK)

        assertThat(store.getLastUploadFailure())
            .isEqualTo(UploadFailure(UploadFailureType.STORAGE_FULL, 100L))
    }

    @Test
    fun `unknown stored failure type reads as no failure`() {
        prefs.edit()
            .putString(RealBackupStatusStore.LAST_FAILURE_TYPE, "NOT_A_TYPE")
            .putLong(RealBackupStatusStore.LAST_FAILURE_AT, 5L)
            .commit()

        assertThat(store.getLastUploadFailure()).isNull()
    }

    @Test
    fun `manual export is stamped with the current time`() {
        nowMillis = 300L
        store.recordManualExport()
        assertThat(store.getLastManualExportAt()).isEqualTo(300L)
    }

    @Test
    fun `seeding sets the baseline once and never moves it`() {
        store.seedDropboxBaselineIfMissing()
        nowMillis = 900L
        store.seedDropboxBaselineIfMissing()

        assertThat(store.getDropboxEnabledAt()).isEqualTo(100L)
    }

    @Test
    fun `connecting resets baseline and clears disconnect flag and failure`() {
        store.seedDropboxBaselineIfMissing()
        store.markDropboxDisconnectedUnexpectedly()
        store.recordDropboxUploadFailure(UploadFailureType.AUTH)

        nowMillis = 400L
        store.recordDropboxConnected()

        assertThat(store.getDropboxEnabledAt()).isEqualTo(400L)
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isFalse()
        assertThat(store.getLastUploadFailure()).isNull()
    }

    @Test
    fun `user disconnect keeps last success but clears the rest`() {
        store.recordDropboxUploadSuccess()
        store.seedDropboxBaselineIfMissing()
        store.markDropboxDisconnectedUnexpectedly()
        store.recordDropboxUploadFailure(UploadFailureType.OTHER)

        store.recordDropboxDisconnectedByUser()

        assertThat(store.getLastDropboxSuccessAt()).isEqualTo(100L)
        assertThat(store.getDropboxEnabledAt()).isNull()
        assertThat(store.wasDropboxDisconnectedUnexpectedly()).isFalse()
        assertThat(store.getLastUploadFailure()).isNull()
    }
}
