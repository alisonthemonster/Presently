package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.settings.RealBackupStatusStore
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupSeverity
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatus
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusResolver
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusType
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupSource
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class BackupStatusCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = 1_000_000_000_000L
    private val day = TimeUnit.DAYS.toMillis(1)

    private fun show(status: BackupStatus, onAction: (TimelineCardAction) -> Unit = {}, onDismiss: () -> Unit = {}): TimelineCardRowState {
        val card = status.toTimelineCard(now)!!
        composeRule.setContent {
            PresentlyTheme {
                TimelineCard(state = card, onActionClick = { onAction(card.action) }, onDismiss = onDismiss)
            }
        }
        return card
    }

    @Test
    fun high_hasNoCloseIconAndDangerTone() {
        listOf(BackupStatus.DropboxDisconnected, BackupStatus.DropboxFull, BackupStatus.DropboxFailing(true, null)).forEach {
            val card = it.toTimelineCard(now)!!
            assertThat(card.isDismissible).isFalse()
            assertThat(card.tone).isEqualTo(TimelineCardTone.DANGER)
        }
        show(BackupStatus.DropboxDisconnected)
        composeRule.onNodeWithTag(TimelineCardTags.CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(TimelineCardTags.ACTION).assertIsDisplayed()
        composeRule.onNodeWithTag(TimelineCardTags.DISMISS).assertDoesNotExist()
    }

    @Test
    fun disconnected_buttonReconnects() {
        var action: TimelineCardAction? = null
        show(BackupStatus.DropboxDisconnected, onAction = { action = it })
        composeRule.onNodeWithTag(TimelineCardTags.ACTION).performClick()
        assertThat(action).isEqualTo(TimelineCardAction.ReconnectDropbox)
    }

    @Test
    fun neverBackedUp_isDismissibleWarning() {
        var dismissed = false
        val card = show(BackupStatus.NeverBackedUp(40), onDismiss = { dismissed = true })
        assertThat(card.tone).isEqualTo(TimelineCardTone.WARNING)
        assertThat(card.action).isEqualTo(TimelineCardAction.OpenSettings)
        composeRule.onNodeWithTag(TimelineCardTags.DISMISS).assertIsDisplayed().performClick()
        assertThat(dismissed).isTrue()
    }

    @Test
    fun stale_isDismissibleAndExports() {
        val status = BackupStatus.Stale(now - 25 * day, BackupSource.MANUAL, 3)
        val card = show(status)
        assertThat(card.isDismissible).isTrue()
        assertThat(card.action).isEqualTo(TimelineCardAction.ExportBackup)
        assertThat(card.title.args).containsExactly(3)
        composeRule.onNodeWithTag(TimelineCardTags.DISMISS).assertIsDisplayed()
    }

    @Test
    fun healthy_hasNoCard() {
        assertThat(BackupStatus.Healthy.toTimelineCard(now)).isNull()
    }

    @Test
    fun everySeverityMapsToACard() {
        assertThat(BackupStatusType.entries.filter { it.severity != BackupSeverity.NONE }).hasSize(5)
    }

    // --- input assembly and persistence ---

    private val settings: PresentlySettings = mock<PresentlySettings>().also {
        whenever(it.getAutomaticBackupCadence()).thenReturn(BackupCadence.DAILY)
    }

    private fun store(clock: () -> Long = { now }) = RealBackupStatusStore(
        androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("card-test-${System.nanoTime()}", android.content.Context.MODE_PRIVATE),
        clock
    )

    @Test
    fun input_countsOnlyEntriesAfterTheBackupDay() {
        val store = store()
        store.recordManualExport()
        val backupDate = java.time.Instant.ofEpochMilli(now).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
        val entries = listOf(
            Entry(backupDate.plusDays(1), "after"),
            Entry(backupDate, "same day"),
            Entry(backupDate.minusDays(3), "before"),
            Entry(backupDate.plusDays(2), "")
        )
        val input = buildBackupStatusInput(settings, store, entries, now)
        assertThat(input.totalEntries).isEqualTo(3)
        assertThat(input.entriesSinceLastBackup).isEqualTo(1)
        assertThat(input.dropboxConnected).isFalse()
    }

    @Test
    fun neverBackedUp_needsTenEntries_andResolvesFromStore() {
        val store = store()
        val few = (1..9).map { Entry(LocalDate.of(2026, 1, it), "x") }
        val many = (1..10).map { Entry(LocalDate.of(2026, 1, it), "x") }
        assertThat(BackupStatusResolver.resolve(buildBackupStatusInput(settings, store, few, now))).isEqualTo(BackupStatus.Healthy)
        assertThat(BackupStatusResolver.resolve(buildBackupStatusInput(settings, store, many, now)))
            .isEqualTo(BackupStatus.NeverBackedUp(10))
    }

    @Test
    fun dismissal_survivesANewStoreAndSnoozes() {
        val prefs = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
            .getSharedPreferences("dismissal-test", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        RealBackupStatusStore(prefs) { now }.recordBannerDismissal(BackupStatusType.NEVER_BACKED_UP)

        val reloaded = RealBackupStatusStore(prefs) { now }.getBannerDismissal()
        val status = BackupStatus.NeverBackedUp(20)
        assertThat(BackupStatusResolver.shouldShowBanner(status, reloaded, now + 13 * day)).isFalse()
        assertThat(BackupStatusResolver.shouldShowBanner(status, reloaded, now + 14 * day)).isTrue()
        // High severity ignores a dismissal.
        assertThat(BackupStatusResolver.shouldShowBanner(BackupStatus.DropboxFull, reloaded, now)).isTrue()
    }
}
