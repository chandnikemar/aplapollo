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

//        val sharedPrefer  = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
//        var accessToken = sharedPrefer.getString(Constants.KEY_JWT_TOKEN, null)

        val originalRequest = chain.request()
        val isLoginEndpoint = originalRequest.url.toString()
            .contains("AuthService/authenticate", ignoreCase = true)
        var token = getToken()
        val requestWithToken = if (isLoginEndpoint) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", token ?: "")
                .build()
        }

        var response = chain.proceed(requestWithToken)

        if (isLoginEndpoint || response.code != 401) {
            return response
        }

        response.close()

        runBlocking {
            val newToken = TokenManager.refreshTokenIfNeeded(context, token)
            if (newToken != null) {
                token = newToken
            } else {
                AuthState.isUnauthorized = true
                return@runBlocking
            }
        }
        token = getToken()
        val newRequest = originalRequest.newBuilder()
            .removeHeader("Authorization")
            .addHeader("Authorization", token ?: "")
            .build()

        return chain.proceed(newRequest)
    }

}
