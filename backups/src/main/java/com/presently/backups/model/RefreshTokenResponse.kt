package com.presently.backups.model

data class RefreshTokenResponse(
    val accessToken: String,
    val expiresIn: Long,
    val refreshToken: String
)