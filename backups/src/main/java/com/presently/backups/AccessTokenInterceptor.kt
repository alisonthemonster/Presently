package com.presently.backups

import okhttp3.Interceptor
import okhttp3.Response

class AccessTokenInterceptor(
    private val dropboxCredentialProvider: DropboxCredentialProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = dropboxCredentialProvider.getAccessToken()

        return if (token == null) {
            chain.proceed(chain.request())
        } else {
            val authenticatedRequest = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(authenticatedRequest)
        }
    }
}