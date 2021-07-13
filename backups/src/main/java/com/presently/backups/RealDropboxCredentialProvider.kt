package com.presently.backups

import com.dropbox.core.oauth.DbxCredential
import com.presently.settings.PresentlySettings
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