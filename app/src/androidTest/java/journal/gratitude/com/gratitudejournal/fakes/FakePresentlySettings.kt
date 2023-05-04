package journal.gratitude.com.gratitudejournal.fakes

import android.content.Context
import com.dropbox.core.oauth.DbxCredential
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class FakePresentlySettings @Inject constructor() : PresentlySettings {

    var fakeTheme = "Original"

    override fun getCurrentTheme(): String {
        return fakeTheme
    }

    override fun setTheme(themeName: String) {
        fakeTheme = themeName
    }

    override fun isBiometricsEnabled(): Boolean {
        return false
    }

    override fun shouldLockApp(): Boolean {
        return false
    }

    override fun onAppBackgrounded() {}

    override fun onAuthenticationSucceeded() {}

    override fun shouldShowQuote(): Boolean {
        return true
    }

    override fun getAutomaticBackupCadence(): BackupCadence {
        return BackupCadence.DAILY
    }

    override fun getLocale(): String {
        return "en-US"
    }

    override fun hasEnabledNotifications(): Boolean {
        return true
    }

    override fun getNotificationTime(): LocalTime {
        return LocalTime.parse("21:00")
    }

    override fun hasUserDisabledAlarmReminders(context: Context): Boolean {
        return false
    }

    override fun getLinesPerEntryInTimeline(): Int {
        return 10
    }

    override fun shouldShowDayOfWeekInTimeline(): Boolean {
        return false
    }

    override fun getAccessToken(): DbxCredential? {
        return null
    }

    override fun setAccessToken(newToken: DbxCredential) {}

    override fun wasDropboxAuthInitiated(): Boolean = false

    override fun markDropboxAuthAsCancelled() {}

    override fun markDropboxAuthInitiated() {}

    override fun clearAccessToken() { }

    override fun isOptedIntoAnalytics(): Boolean = true
}
