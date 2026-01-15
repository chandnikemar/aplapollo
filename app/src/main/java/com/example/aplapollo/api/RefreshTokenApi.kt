package com.example.aplapollo.api

import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.refreshtoken.RefreshTokenRequest
import com.example.aplapollo.helper.refreshtoken.RefreshTokenResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshTokenApi {
    @POST(Constants.REFRESH_TOKEN_DATA)
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}