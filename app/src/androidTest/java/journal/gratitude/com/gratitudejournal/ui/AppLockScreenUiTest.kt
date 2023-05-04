package journal.gratitude.com.gratitudejournal.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import com.presently.settings.wiring.PresentlySettingsModule
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dagger.hilt.components.SingletonComponent
import journal.gratitude.com.gratitudejournal.MainActivity
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import kotlin.test.fail

@UninstallModules(PresentlySettingsModule::class)
@HiltAndroidTest
class AppLockScreenUiTest {

    @Module
    @InstallIn(SingletonComponent::class)
    object TestPresentlySettingsModule {

        // fake that the user has biometrics enabled
        @Provides
        fun providesPresentlySettings(): PresentlySettings {
            return object : PresentlySettings {
                override fun getCurrentTheme(): String = "Original"
                override fun isBiometricsEnabled(): Boolean = true
                override fun shouldLockApp(): Boolean = true

                override fun setTheme(themeName: String) = fail("Should not use in this test")
                override fun onAppBackgrounded() {}
                override fun onAuthenticationSucceeded() = fail("Should not use in this test")
                override fun shouldShowQuote(): Boolean = fail("Should not use in this test")
                override fun getAutomaticBackupCadence(): BackupCadence = fail("Should not use in this test")
                override fun getLocale(): String = "en-US"
                override fun hasEnabledNotifications(): Boolean = true
                override fun getNotificationTime(): LocalTime = LocalTime.parse("21:00")
                override fun hasUserDisabledAlarmReminders(context: Context): Boolean = false
                override fun getLinesPerEntryInTimeline(): Int = fail("Should not use in this test")
                override fun shouldShowDayOfWeekInTimeline(): Boolean = fail("Should not use in this test")
                override fun getAccessToken(): DbxCredential? = fail("Should not use in this test")
                override fun setAccessToken(newToken: DbxCredential) = fail("Should not use in this test")
                override fun wasDropboxAuthInitiated(): Boolean = fail("Should not use in this test")
                override fun markDropboxAuthAsCancelled() = fail("Should not use in this test")
                override fun markDropboxAuthInitiated() = fail("Should not use in this test")
                override fun clearAccessToken() = fail("Should not use in this test")
                override fun isOptedIntoAnalytics(): Boolean = fail("Should not use in this test")
            }
        }
    }

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLockScreen() {
        try {
            composeTestRule.onRoot()
        } catch (e: Exception) {
            // biometrics cannot run on emulators so the activity should finish
            // meaning we have no compose hierarchies in the app
            assertThat(e).isInstanceOf(IllegalStateException::class.java)
        }
    }
}
