package com.presently.backups

import com.dropbox.core.oauth.DbxCredential

interface DropboxCredentialProvider {

    //get the short lived access token, may need refreshing
    fun getAccessToken(): String?

    fun refreshTokens(): String?
}