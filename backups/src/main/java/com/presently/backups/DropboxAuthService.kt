package com.presently.backups

import com.presently.backups.model.RefreshTokenResponse
import retrofit2.http.Field
import retrofit2.http.POST

/**
 * Used to authenticate and de-authenticate users
 */
interface DropboxAuthService {

    //TODO do we need the app key and app secret?
    //TODO can we have this suspend?
    @POST("oauth2/token")
    fun refreshToken(
        @Field("refresh_token") refreshToken: String,
        @Field("grant_type") grantType: String = "refresh_token"
    ): RefreshTokenResponse

}

