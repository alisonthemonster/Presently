package com.presently.backups

import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.presently.backups.model.RefreshTokenResponse
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import org.junit.Test
import org.threeten.bp.LocalTime

class RealDropboxCredentialProviderTest {

    var getAccessTokenWasCalled = false
    var setAccessTokenWasCalled = false
    var refreshTokenPassedIn = ""

    @Test
    fun `GIVEN RealDropboxCredentialProvider WHEN getAccessToken is called THEN the presently settings is called`() {
        getAccessTokenWasCalled = false
        val expected = "accessToken"

        val provider = RealDropboxCredentialProvider(presentlySettings, authService)
        val actual = provider.getAccessToken()

        assertThat(getAccessTokenWasCalled).isTrue()
        assertThat(actual).isEqualTo(expected)
    }
    
    @Test
    fun `GIVEN RealDropboxCredentialProvider WHEN refreshTokens is called THEN the presently settings is called`() {
        getAccessTokenWasCalled = false
        setAccessTokenWasCalled = false

        val expected = "newAccessToken"
        val provider = RealDropboxCredentialProvider(presentlySettings, authService)
        val actual = provider.refreshTokens()

        assertThat(getAccessTokenWasCalled).isTrue()
        assertThat(setAccessTokenWasCalled).isTrue()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN RealDropboxCredentialProvider WHEN refreshTokens is called THEN the api is called`() {
        refreshTokenPassedIn = ""

        val provider = RealDropboxCredentialProvider(presentlySettings, authService)
        val actual = provider.refreshTokens()

        assertThat(refreshTokenPassedIn).isEqualTo("refreshToken")
        assertThat(actual).isEqualTo("newAccessToken")
    }

    private val authService = object : DropboxAuthService {
        override fun refreshToken(refreshToken: String, grantType: String): RefreshTokenResponse {
            refreshTokenPassedIn = refreshToken
            return RefreshTokenResponse("newAccessToken", 1000, "refreshToken")
        }

    }

    private val presentlySettings = object : PresentlySettings {
        override fun getCurrentTheme(): String = ""

        override fun setTheme(themeName: String) {}

        override fun isBiometricsEnabled(): Boolean = false

        override fun shouldLockApp(): Boolean = false

        override fun setOnPauseTime() {}

        override fun getFirstDayOfWeek(): Int = 1

        override fun shouldShowQuote(): Boolean = false

        override fun getAutomaticBackupCadence(): BackupCadence = BackupCadence.DAILY

        override fun getLocale(): String = "en-US"

        override fun hasEnabledNotifications(): Boolean = false

        override fun getNotificationTime(): LocalTime = LocalTime.NOON

        override fun getLinesPerEntryInTimeline(): Int = 10

        override fun shouldShowDayOfWeekInTimeline(): Boolean = false

        override fun getAccessToken(): DbxCredential? {
            getAccessTokenWasCalled = true
            return DbxCredential("accessToken", 100L, "refreshToken", "app-key")
        }

        override fun setAccessToken(newToken: DbxCredential) {
            setAccessTokenWasCalled = true
        }

        override fun markDropboxAuthInitiated() {}

        override fun clearAccessToken() {}

        override fun getDropboxAppKey(): String = "app-key"
    }
}