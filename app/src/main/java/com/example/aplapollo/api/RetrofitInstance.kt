package com.example.aplapollo.api

import com.example.aplapollo.helper.AppContextProvider
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.SessionManager
import com.example.aplapollo.helper.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object RetrofitInstance {

    // ===================== SSL (same as yours) =====================
    private fun sslSocketFactory(): Pair<SSLSocketFactory, X509TrustManager> {

        val trustManager = object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), SecureRandom())

        return sslContext.socketFactory to trustManager
    }

    private fun logging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

    // ===================== LOGIN (NO JWT, NO AUTHENTICATOR) =====================
    fun loginApi(baseUrl: String): APLAPOLLOAPI {

        val (sslFactory, trustManager) = sslSocketFactory()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging())
            .sslSocketFactory(sslFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl + Constants.tgsAPi)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APLAPOLLOAPI::class.java)
    }

    // ===================== SERVICE API (JWT + REFRESH) =====================
    fun serviceApi(baseUrl: String): APLAPOLLOAPI {

        val sessionManager = SessionManager(AppContextProvider.context)
        val (sslFactory, trustManager) = sslSocketFactory()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging())
            .addInterceptor(AuthInterceptor(sessionManager)) // ✅ JWT
            .authenticator(
                TokenAuthenticator(
                    sessionManager = sessionManager,
                    baseUrl = baseUrl
                )
            )
            .sslSocketFactory(sslFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl + Constants.serviceAPi)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APLAPOLLOAPI::class.java)
    }

    // ===================== REFRESH TOKEN (NO JWT, NO AUTHENTICATOR) =====================
    fun refreshTokenApi(baseUrl: String): APLAPOLLOAPI {

        val (sslFactory, trustManager) = sslSocketFactory()

        val client = OkHttpClient.Builder()
            .addInterceptor(logging())
            .sslSocketFactory(sslFactory, trustManager)
            .hostnameVerifier { _, _ -> true }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(baseUrl + Constants.serviceAPi)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APLAPOLLOAPI::class.java)
    }
}
