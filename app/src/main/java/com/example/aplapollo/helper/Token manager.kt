package com.example.aplapollo.helper

import android.util.Log
import com.example.aplapollo.api.RetrofitInstance
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val sessionManager: SessionManager,
    private val baseUrl: String
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {

        // 🚫 Stop infinite retry
        if (responseCount(response) >= 2) {
            Log.e("AUTH", "❌ Multiple 401 detected → Force logout")
            forceLogout()
            return null
        }

        val oldJwt = sessionManager.getJWTToken()
        val refreshToken = sessionManager.getRefreshToken()

        Log.e("AUTH", "⚠️ 401 Received")
        Log.d("AUTH", "Old JWT = $oldJwt")
        Log.d("AUTH", "Refresh Token = $refreshToken")

        if (refreshToken.isNullOrEmpty()) {
            Log.e("AUTH", "❌ Refresh token missing")
            forceLogout()
            return null
        }

        return try {
            Log.d("AUTH", "🔁 Calling REFRESH TOKEN API")

            // ✅ USE SEPARATE REFRESH API (NO AUTHENTICATOR)
            val refreshResponse = RetrofitInstance
                .refreshTokenApi(baseUrl)
                .refreshToken(mapOf("refreshToken" to refreshToken))
                .execute()

            Log.d("AUTH", "Refresh API Code = ${refreshResponse.code()}")

            if (refreshResponse.isSuccessful) {

                val newJwt = refreshResponse.body()?.jwtToken
                Log.d("AUTH", "✅ New JWT = $newJwt")

                if (!newJwt.isNullOrEmpty()) {

                    sessionManager.updateJwtToken(newJwt)

                    // 🔁 Retry original request with new JWT
                    response.request.newBuilder()
                        .header("Authorization", "Bearer $newJwt")
                        .build()
                } else {
                    Log.e("AUTH", "❌ New JWT is null")
                    forceLogout()
                    null
                }

            } else {
                Log.e(
                    "AUTH",
                    "❌ Refresh failed | Code=${refreshResponse.code()} | Message=${refreshResponse.errorBody()?.string()}"
                )
                forceLogout()
                null
            }

        } catch (e: Exception) {
            Log.e("AUTH", "❌ Exception during refresh", e)
            forceLogout()
            null
        }
    }

    // Count retry attempts
    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }

    private fun forceLogout() {
        sessionManager.logoutKeepAdminConfig()
        SessionExpiredEvent.post() // 🔥 Observed in Activity → Login
    }
}
