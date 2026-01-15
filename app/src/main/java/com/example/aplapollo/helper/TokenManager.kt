package com.example.aplapollo.helper

import android.content.Context
import android.content.Intent
import com.example.aplapollo.api.RefreshTokenApi
import com.example.aplapollo.helper.refreshtoken.RefreshTokenRequest
import com.example.aplapollo.helper.refreshtoken.RefreshTokenResponse
import com.example.aplapollo.view.LoginActivity


import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object TokenManager {
    private val mutex = Mutex()
    private var refreshDeferred: CompletableDeferred<Boolean>? = null

    suspend fun refreshTokenIfNeeded(context: Context, oldToken: String?): String? {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)

        return mutex.withLock {
            val currentToken = prefs.getString(Constants.KEY_JWT_TOKEN, null)
            if (currentToken != oldToken) {
                return currentToken // Already refreshed
            }

            // If another refresh is already happening, wait for it
            refreshDeferred?.let {
                val success = it.await()
                return if (success) prefs.getString(Constants.KEY_JWT_TOKEN, null) else null
            }

            // This thread will perform the refresh
            refreshDeferred = CompletableDeferred<Boolean>()

            val refreshToken = prefs.getString(Constants.KEY_Refresh_Token, null)
            val baseUrl =prefs.getString(Constants.BASE_URL, null).toString()

            val newToken = refreshAccessToken(refreshToken, baseUrl+"/api/")

            return if (newToken != null) {
                prefs.edit().putString(Constants.KEY_JWT_TOKEN, newToken.jwtToken).apply()
                prefs.edit().putString(Constants.KEY_Refresh_Token, newToken.refreshToken).apply()
                refreshDeferred?.complete(true)
                newToken.jwtToken
            } else {
                AuthState.isUnauthorized = true

                prefs.edit().putBoolean(Constants.KEY_ISLOGGEDIN, false).apply()
                redirectToLogin(context)
                refreshDeferred?.complete(false)
                null
            }.also {
                refreshDeferred = null
            }
        }
    }

    private fun refreshAccessToken(refreshToken: String?, baseUrl: String): RefreshTokenResponse? {
        if (refreshToken.isNullOrEmpty()) return null
        // ✅ Trust all certificates (for development only)
        val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
            object : javax.net.ssl.X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
        val sslSocketFactory = sslContext.socketFactory
        // ✅ Logging interceptor
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // ✅ Build OkHttp client with SSL and hostname verifier
        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
            .hostnameVerifier { _, _ -> true } // allow all hostnames
            .addInterceptor(logging)
            .build()

        // ✅ Retrofit setup
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val refreshTokenApi = retrofit.create(RefreshTokenApi::class.java)

        return try {
            val response = refreshTokenApi.refreshToken(RefreshTokenRequest(refreshToken)).execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun redirectToLogin(context: Context) {
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }
}
