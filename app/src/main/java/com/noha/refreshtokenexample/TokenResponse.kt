package com.noha.refreshtokenexample

data class TokenResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val lastUpdatedTime: Long
)
