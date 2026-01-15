package com.example.aplapollo.helper.refreshtoken

data class RefreshTokenResponse(
    val jwtToken: String,
    val refreshToken: String
)