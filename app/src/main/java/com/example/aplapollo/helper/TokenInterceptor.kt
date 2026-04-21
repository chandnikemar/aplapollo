package com.example.aplapollo.helper

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response


object AuthState {
    @Volatile var isUnauthorized = false
}

class TokenInterceptor(
    private val context: Context
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        fun getToken(): String? {
            val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
            return prefs.getString(Constants.KEY_JWT_TOKEN, null)
        }

        val originalRequest = chain.request()

        val isLoginEndpoint = originalRequest.url.toString()
            .contains("AuthService/authenticate", ignoreCase = true)

        val isRetry = originalRequest.header("Retry") != null

        val token = getToken()

        val requestWithToken = if (isLoginEndpoint || token.isNullOrEmpty()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(requestWithToken)

        // ✅ If not 401 → return directly (DO NOT TOUCH)
        if (isLoginEndpoint || response.code != 401 || isRetry) {
            return response
        }

        // 🔁 Token refresh (NO close here yet)
        val newToken = runBlocking {
            TokenManager.refreshTokenIfNeeded(context, token)
        }

        if (newToken.isNullOrEmpty()) {
            AuthState.isUnauthorized = true
            return response // ✅ return original response safely
        }

        // ❗ Now close old response ONLY because we retry
        response.close()

        val newRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", "Bearer $newToken")
            .addHeader("Retry", "true")
            .build()

        return chain.proceed(newRequest)
    }
}