package journal.gratitude.com.gratitudejournal.ui.timeline

import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureInTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupSource
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatus
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * The backup status card at the top of the timeline, one scenario per severity, in the default
 * theme and, for two of them, a dark theme (the card's tone is fixed, so every theme isn't needed).
 *
 * Baselines are recorded on CI (see the `record_screenshots` pipeline parameter), not locally.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class TimelineCardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = 1_800_000_000_000L
    private val day = TimeUnit.DAYS.toMillis(1)

    @Test
    fun dropboxDisconnected_high() = capture("dropbox-disconnected", BackupStatus.DropboxDisconnected)

    @Test
    fun dropboxDisconnected_high_dark() =
        capture("dropbox-disconnected", BackupStatus.DropboxDisconnected, PresentlyThemeSpec.Midnight)

    @Test
    fun dropboxFull_high() = capture("dropbox-full", BackupStatus.DropboxFull)

    @Test
    fun neverBackedUp_medium() = capture("never-backed-up", BackupStatus.NeverBackedUp(40))

    @Test
    fun neverBackedUp_medium_dark() =
        capture("never-backed-up", BackupStatus.NeverBackedUp(40), PresentlyThemeSpec.Midnight)

    @Test
    fun stale_low() =
        capture("stale", BackupStatus.Stale(now - 25 * day, BackupSource.MANUAL, 6))

    private fun capture(
        scenario: String,
        status: BackupStatus,
        theme: PresentlyThemeSpec = PresentlyThemeSpec.Original
    ) {
        val card = status.toTimelineCard(now)!!
        run {
            composeRule.captureInTheme(
                screen = "timeline-card",
                scenario = scenario,
                themeSpec = theme
            ) {
                TimelineScreenContent(
                    state = TimelineUiState(
                        items = listOf(
                            card,
                            // Today and yesterday are always present, with their placeholders.
                            TimelineEntryRowState(
                                date = LocalDate.of(2026, 3, 30),
                                dateText = "March 30, 2026",
                                content = "",
                                emptyHint = R.string.what_are_you_thankful_for_today,
                                isCurrentDate = true,
                                isNewEntry = true,
                                numberExistingEntries = 5,
                                maxLines = 10,
                                isLastItem = false
                            ),
                            TimelineEntryRowState(
                                date = LocalDate.of(2026, 3, 29),
                                dateText = "March 29, 2026",
                                content = "",
                                emptyHint = R.string.what_are_you_thankful_for_yesterday,
                                isCurrentDate = false,
                                isNewEntry = true,
                                numberExistingEntries = 5,
                                maxLines = 10,
                                isLastItem = false
                            ),
                            TimelineEntryRowState(
                                date = LocalDate.of(2026, 3, 28),
                                dateText = "March 28, 2026",
                                content = "Sunshine on the balcony",
                                emptyHint = null,
                                isCurrentDate = false,
                                isNewEntry = false,
                                numberExistingEntries = 5,
                                maxLines = 10,
                                isLastItem = true
                            )
                        )
                    ),
                    onSearchClick = {},
                    onSettingsClick = {},
                    onTimelineEntryClick = {},
                    onCalendarClick = {},
                    onCalendarClose = {},
                    onCalendarDateClick = { _, _, _ -> }
                )
            }
        }
    }
}
