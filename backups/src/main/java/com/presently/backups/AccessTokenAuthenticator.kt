package com.presently.backups

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

/**
 * OkHttp Authenticator to handle 401 errors and refreshes tokens. Inspired by this post:
 * https://blog.coinbase.com/okhttp-oauth-token-refreshes-b598f55dd3b2
 */
class AccessTokenAuthenticator(val dropboxCredentialProvider: DropboxCredentialProvider): Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? {
        //if we don't have any tokens we cannot refresh
        val token = dropboxCredentialProvider.getAccessToken() ?: return null

        synchronized(this) {
            val newToken = dropboxCredentialProvider.getAccessToken()

            // Check if the request made was previously made as an authenticated request.
            if (response.request.header("Authorization") != null) {
                // If the token has changed since the request was made, use the new token.
                if (newToken != token) {
                    return response.request
                        .newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $newToken")
                        .build()
                }

                val updatedToken = dropboxCredentialProvider.refreshTokens() ?: return null

                // Retry the request with the new token.
                return response.request
                    .newBuilder()
                    .removeHeader("Authorization")
                    .addHeader("Authorization", "Bearer $updatedToken")
                    .build()
            }
        }
        return null

    }
}