package com.presently.backups.model

import com.google.gson.annotations.SerializedName

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("expires_in")val expiresIn: Long,
    val refreshToken: String
)