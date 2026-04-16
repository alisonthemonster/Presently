package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureInTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * To update screenshots: ./gradlew :app:recordRoborazziDebug -Pscreenshot --tests journal.gratitude.com.gratitudejournal.ui.settings.BackupSettingsScreenRoborazziTest
 *
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class BackupSettingsScreenRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun mixedProviderConnectionState_originalTheme() {
        composeRule.captureInTheme(
            screen = "backup-settings",
            scenario = "dropbox-connected-google-disconnected",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            BackupSettingsScreen(
                state = BackupSettingsUiState(
                    dropbox = BackupProviderUiState(
                        isConnected = true,
                        accountEmail = "dropbox.user@example.com",
                        lastBackupTimestamp = 1_776_171_600_000L,
                        frequency = BackupFrequency.MONTHLY
                    ),
                    googleDrive = BackupProviderUiState(
                        isConnected = false,
                        accountEmail = null,
                        lastBackupTimestamp = null,
                        frequency = BackupFrequency.DAILY
                    )
                ),
                onBackClick = {},
                onDropboxConnectClick = {},
                onDropboxDisconnectClick = {},
                onDropboxFrequencySelected = {},
                onGoogleDriveConnectClick = {},
                onGoogleDriveDisconnectClick = {},
                onGoogleDriveFrequencySelected = {},
                onImportClick = {},
                onImportDismissed = {},
                onImportConfirmed = {},
                onExportClick = {},
                onBackupGuideClick = {},
                onRestoreDismissed = {},
                onRestoreConfirmed = {}
            )
        }
    }
}
