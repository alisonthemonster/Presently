package com.presently.backups

import com.presently.backups.model.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.*

/**
 * Used to authenticate and de-authenticate users
 */
interface DropboxAuthService {

    @POST("oauth2/token")
    @FormUrlEncoded
    fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("client_id") appKey: String,
        @Field("refresh_token") refreshToken: String,
    ): Call<RefreshTokenResponse>

}

