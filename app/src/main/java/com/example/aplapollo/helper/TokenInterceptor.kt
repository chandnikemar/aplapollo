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
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        var accessToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)

        val originalRequest = chain.request()
        val isLoginEndpoint = originalRequest.url.toString()
            .contains("UserManagement/authenticate", ignoreCase = true)

        val requestWithToken = if (isLoginEndpoint) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", accessToken ?: "")
                .build()
        }

        var response = chain.proceed(requestWithToken)

        if (isLoginEndpoint || response.code != 401) {
            return response
        }

        response.close()

        runBlocking {
            val newToken = TokenManager.refreshTokenIfNeeded(context, accessToken)
            if (newToken != null) {
                accessToken = newToken
            } else {
                AuthState.isUnauthorized = true
                return@runBlocking
            }
        }

        val newRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", accessToken ?: "")
            .build()

        return chain.proceed(newRequest)
    }

}
