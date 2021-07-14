package com.presently.backups

import com.dropbox.core.oauth.DbxCredential
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.presently.settings.PresentlySettings
import java.lang.IllegalStateException
import javax.inject.Inject

class RealDropboxCredentialProvider @Inject constructor(
    private val presentlySettings: PresentlySettings,
    private val dropboxAuthService: DropboxAuthService
) : DropboxCredentialProvider {
    override fun getAccessToken(): String? {
        return presentlySettings.getAccessToken()?.accessToken
    }

    override fun refreshTokens(): String? {
        val existingToken = requireNotNull(presentlySettings.getAccessToken())
        if (existingToken.refreshToken == null) {
            //this is most likely a user with a long term access token
            //users who auth after Sept 30, 2021 will not get long term access tokens
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(IllegalStateException("Trying to refresh tokens for a user without refresh tokens"))
            return null
        }

        //make synchronous network call with refresh token to get new access token
        val result = dropboxAuthService.refreshToken(
            refreshToken = existingToken.refreshToken,
            appKey = presentlySettings.getDropboxAppKey()
        ).execute().body()
            ?: return null
        val credential = DbxCredential(
            result.accessToken,
            result.expiresIn,
            existingToken.refreshToken,
            presentlySettings.getDropboxAppKey()
        )

        //store new token in settings and return access token
        presentlySettings.setAccessToken(credential)
        return credential.accessToken
    }
}