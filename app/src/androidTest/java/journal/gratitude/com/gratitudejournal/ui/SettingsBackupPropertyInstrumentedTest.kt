package journal.gratitude.com.gratitudejournal.ui

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dropbox.core.oauth.DbxCredential
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.model.BACKUP_CADENCE
import journal.gratitude.com.gratitudejournal.testUtils.launchFragmentInHiltContainer
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * The cadence is changed by the preference framework rather than through PresentlySettings, so
 * SettingsFragment is what keeps the dropbox_backup user property in step with it.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class SettingsBackupPropertyInstrumentedTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settings: FakePresentlySettings

    @Inject
    lateinit var analytics: FakeAnalyticsLogger

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun changingCadenceWhileConnected_updatesTheProperty() {
        settings.accessTokenValue = DbxCredential("token")
        settings.cadenceValue = BackupCadence.WEEKLY

        launchFragmentInHiltContainer<SettingsFragment>(
            action = { (this as SettingsFragment).onSharedPreferenceChanged(null, BACKUP_CADENCE) }
        )

        assertEquals("weekly", analytics.userProperties["dropbox_backup"])
    }

    @Test
    fun changingCadenceWhileNotConnected_leavesThePropertyOff() {
        settings.accessTokenValue = null
        settings.cadenceValue = BackupCadence.EVERY_CHANGE

        launchFragmentInHiltContainer<SettingsFragment>(
            action = { (this as SettingsFragment).onSharedPreferenceChanged(null, BACKUP_CADENCE) }
        )

        assertEquals("off", analytics.userProperties["dropbox_backup"])
    }
}
