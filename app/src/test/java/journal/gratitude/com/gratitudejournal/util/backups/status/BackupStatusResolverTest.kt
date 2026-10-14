package journal.gratitude.com.gratitudejournal.util.backups.status

import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import org.junit.Test
import java.util.concurrent.TimeUnit

class BackupStatusResolverTest {

    private val now = TimeUnit.DAYS.toMillis(1000)

    private fun hours(h: Long) = TimeUnit.HOURS.toMillis(h)
    private fun days(d: Long) = TimeUnit.DAYS.toMillis(d)

    private fun input(
        dropboxConnected: Boolean = false,
        disconnectedUnexpectedly: Boolean = false,
        cadence: BackupCadence = BackupCadence.DAILY,
        enabledAt: Long? = null,
        lastSuccess: Long? = null,
        failure: UploadFailure? = null,
        lastManualExport: Long? = null,
        totalEntries: Int = 0,
        entriesSinceExport: Int = 0
    ) = BackupStatusInput(
        nowMillis = now,
        dropboxConnected = dropboxConnected,
        dropboxDisconnectedUnexpectedly = disconnectedUnexpectedly,
        cadence = cadence,
        dropboxEnabledAtMillis = enabledAt,
        lastDropboxSuccessAtMillis = lastSuccess,
        lastUploadFailure = failure,
        lastManualExportAtMillis = lastManualExport,
        totalEntries = totalEntries,
        entriesSinceLastManualExport = entriesSinceExport
    )

    // --- not connected -------------------------------------------------

    @Test
    fun `GIVEN tiny journal and no backup THEN healthy`() {
        val status = BackupStatusResolver.resolve(input(totalEntries = 9))
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN 10 entries and never exported THEN never backed up`() {
        val status = BackupStatusResolver.resolve(input(totalEntries = 10))
        assertThat(status).isEqualTo(BackupStatus.NeverBackedUp(10))
    }

    @Test
    fun `GIVEN old export with new entries THEN stale`() {
        val export = now - days(31)
        val status = BackupStatusResolver.resolve(
            input(lastManualExport = export, totalEntries = 50, entriesSinceExport = 12)
        )
        assertThat(status).isEqualTo(BackupStatus.Stale(export, 12))
    }

    @Test
    fun `GIVEN old export but no new entries THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(lastManualExport = now - days(90), totalEntries = 50, entriesSinceExport = 0)
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN recent export with new entries THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(lastManualExport = now - days(29), totalEntries = 50, entriesSinceExport = 5)
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    // --- dropbox -------------------------------------------------------

    @Test
    fun `GIVEN unexpected disconnect THEN disconnected even though not connected`() {
        val status = BackupStatusResolver.resolve(
            input(disconnectedUnexpectedly = true, totalEntries = 100)
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxDisconnected)
    }

    @Test
    fun `GIVEN storage full failure THEN full`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = now - days(2),
                failure = UploadFailure(UploadFailureType.STORAGE_FULL, now - hours(1))
            )
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxFull)
    }

    @Test
    fun `GIVEN failed last attempt THEN failing with last attempt failed`() {
        val success = now - days(2)
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = success,
                failure = UploadFailure(UploadFailureType.OTHER, now - hours(1))
            )
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxFailing(true, success))
    }

    @Test
    fun `GIVEN transient network failure within schedule THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = now - hours(20),
                failure = UploadFailure(UploadFailureType.NETWORK, now - hours(1))
            )
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN transient failure and overdue THEN failing`() {
        val success = now - hours(40)
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = success,
                failure = UploadFailure(UploadFailureType.TRANSIENT_SERVER, now - hours(1))
            )
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxFailing(false, success))
    }

    @Test
    fun `GIVEN failure older than latest success THEN failure is ignored`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = now - hours(1),
                failure = UploadFailure(UploadFailureType.OTHER, now - hours(5))
            )
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN daily cadence within grace THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(dropboxConnected = true, lastSuccess = now - hours(35))
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN daily cadence past grace THEN failing overdue`() {
        val success = now - hours(37)
        val status = BackupStatusResolver.resolve(
            input(dropboxConnected = true, lastSuccess = success)
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxFailing(false, success))
    }

    @Test
    fun `GIVEN weekly cadence 6 days since success THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                cadence = BackupCadence.WEEKLY,
                lastSuccess = now - days(6)
            )
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN weekly cadence past grace THEN failing overdue`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                cadence = BackupCadence.WEEKLY,
                lastSuccess = now - days(8)
            )
        )
        assertThat(status).isInstanceOf(BackupStatus.DropboxFailing::class.java)
    }

    @Test
    fun `GIVEN every change cadence and very old success THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                cadence = BackupCadence.EVERY_CHANGE,
                lastSuccess = now - days(60)
            )
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN freshly enabled dropbox with no success yet THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(dropboxConnected = true, enabledAt = now - hours(2))
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN enabled long ago with no success THEN failing overdue`() {
        val status = BackupStatusResolver.resolve(
            input(dropboxConnected = true, enabledAt = now - days(3))
        )
        assertThat(status).isEqualTo(BackupStatus.DropboxFailing(false, null))
    }

    @Test
    fun `GIVEN recently re-enabled after old success THEN baseline is enable time`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                enabledAt = now - hours(1),
                lastSuccess = now - days(30)
            )
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    @Test
    fun `GIVEN recent manual export but dropbox failing THEN still failing`() {
        val status = BackupStatusResolver.resolve(
            input(
                dropboxConnected = true,
                lastSuccess = now - days(5),
                lastManualExport = now - hours(1),
                totalEntries = 50
            )
        )
        assertThat(status).isInstanceOf(BackupStatus.DropboxFailing::class.java)
    }

    @Test
    fun `GIVEN dropbox connected and healthy but no manual export THEN healthy`() {
        val status = BackupStatusResolver.resolve(
            input(dropboxConnected = true, lastSuccess = now - hours(2), totalEntries = 500)
        )
        assertThat(status).isEqualTo(BackupStatus.Healthy)
    }

    // --- banner visibility --------------------------------------------

    @Test
    fun `healthy never shows banner`() {
        assertThat(BackupStatusResolver.shouldShowBanner(BackupStatus.Healthy, null, now)).isFalse()
    }

    @Test
    fun `high severity ignores dismissal`() {
        val dismissal = BackupBannerDismissal(BackupStatusType.DROPBOX_FAILING, now - 1)
        val status = BackupStatus.DropboxFailing(true, null)
        assertThat(BackupStatusResolver.shouldShowBanner(status, dismissal, now)).isTrue()
    }

    @Test
    fun `medium dismissal hides within snooze and shows after`() {
        val status = BackupStatus.NeverBackedUp(20)
        val dismissal = BackupBannerDismissal(BackupStatusType.NEVER_BACKED_UP, now - days(13))
        assertThat(BackupStatusResolver.shouldShowBanner(status, dismissal, now)).isFalse()

        val old = dismissal.copy(atMillis = now - days(14))
        assertThat(BackupStatusResolver.shouldShowBanner(status, old, now)).isTrue()
    }

    @Test
    fun `low dismissal snoozes for 30 days`() {
        val status = BackupStatus.Stale(now - days(40), 3)
        val dismissal = BackupBannerDismissal(BackupStatusType.STALE, now - days(29))
        assertThat(BackupStatusResolver.shouldShowBanner(status, dismissal, now)).isFalse()
    }

    @Test
    fun `dismissal of a different state does not hide the banner`() {
        val status = BackupStatus.Stale(now - days(40), 3)
        val dismissal = BackupBannerDismissal(BackupStatusType.NEVER_BACKED_UP, now - days(1))
        assertThat(BackupStatusResolver.shouldShowBanner(status, dismissal, now)).isTrue()
    }

    @Test
    fun `no dismissal shows banner`() {
        val status = BackupStatus.NeverBackedUp(20)
        assertThat(BackupStatusResolver.shouldShowBanner(status, null, now)).isTrue()
    }
}
