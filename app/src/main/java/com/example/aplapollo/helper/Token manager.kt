package com.example.aplapollo.helper

//class TokenManager(context: Context) {
//
//    private var token: String? = null
//    private val mutex = Mutex() // For thread safety
//    private val aplRepository =APLRepository () // Passing the context to KYMSRepository
//
//
//    // This function returns the current token, refreshing it if necessary
//    suspend fun getToken(baseUrl: String): String? {
//        mutex.withLock {
//            if (token == null || isTokenExpired()) {
//                refreshTokenIfNeeded(baseUrl)
//            }
//            return token
//        }
//    }
//
//    // Refresh the token using the repository
//    private suspend fun refreshTokenIfNeeded(baseUrl: String) {
//        try {
//            val newToken = aplRepository.refreshTokenIfNeeded(baseUrl, ) // Passing sessionManager
//
//            // Store the new token
//            token = newToken
//
//            // Optionally print the new token for debugging
//            println("Refreshed JWT Token: $token")
//        } catch (e: Exception) {
//            // Handle any errors that may occur during the token refresh
//            println("Error refreshing token: ${e.message}, Cause: ${e.cause}")
//        }
//    }
//
//    // This function checks if the token is expired based on its expiration time
//    private fun isTokenExpired(): Boolean {
//        token?.let {
//            val parts = it.split(".")
//            if (parts.size == 3) {
//                // Decode JWT to get expiration time
//                try {
//                    val payload = String(android.util.Base64.decode(parts[1], android.util.Base64.URL_SAFE))
//                    val json = JSONObject(payload)
//                    val exp = json.optLong("exp", 0)
//                    return System.currentTimeMillis() / 1000 >= exp // Compare expiration time in seconds
//                } catch (e: Exception) {
//                    // Handle any JSON or decoding errors
//                    println("Error decoding JWT: ${e.message}")
//                    return true
//                }
//            }
//        }
//        return true
//    }
//}


//// Global utility function to get a refreshed token
//suspend fun getRefreshedToken(context: Context, baseUrl: String): String? {
//
//    val kymsRepository = KYMSRepository() // Pass context to KYMSRepository
//
//    return try {
//        val newToken = kymsRepository.refreshTokenIfNeeded(baseUrl, ) // Pass sessionManager
//        println("Refreshed JWT Token: $newToken")
//        newToken
//    } catch (e: Exception) {
//        println("Error refreshing token: ${e.message}, Cause: ${e.cause}")
//        null
//    }
//}


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
        sessionManager.clearSharedPrefs()
        SessionExpiredEvent.post() // 🔥 Observed in Activity → Login
    }
}
