package com.presently.backups

import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import com.presently.backups.model.RefreshTokenResponse
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import okhttp3.Request
import okio.Timeout
import org.junit.Test
import org.threeten.bp.LocalTime
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        override fun refreshToken(
            grantType: String,
            appKey: String,
            refreshToken: String
        ): Call<RefreshTokenResponse> {
            refreshTokenPassedIn = refreshToken
            val response = RefreshTokenResponse("newAccessToken", 1000, "refreshToken")
            val call = object : Call<RefreshTokenResponse> {
                override fun clone(): Call<RefreshTokenResponse> {
                    TODO("Not yet implemented")
                }

                override fun execute(): Response<RefreshTokenResponse> {
                    return Response.success(response)
                }

                override fun enqueue(callback: Callback<RefreshTokenResponse>) {
                    TODO("Not yet implemented")
                }

                override fun isExecuted(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun cancel() {
                    TODO("Not yet implemented")
                }

                override fun isCanceled(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun request(): Request {
                    TODO("Not yet implemented")
                }

                override fun timeout(): Timeout {
                    TODO("Not yet implemented")
                }

            }
            return call
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