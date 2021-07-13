package com.presently.backups

import com.dropbox.core.oauth.DbxCredential

interface DropboxCredentialProvider {

    /**
     * Returns the short lived access token.
     */
    fun getAccessToken(): String?

    /**
     * Refreshes the token and returns it. This call should be made synchronously.
     * In the event that the token could not be refreshed return null.
     */
    fun refreshTokens(): String?
}