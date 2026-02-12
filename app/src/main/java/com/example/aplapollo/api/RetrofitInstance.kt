package com.example.aplapollo.api

import android.content.Context
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.X509TrustManager
class RetrofitInstance private constructor(private val context: Context) {

    companion object {

        @Volatile
        private var INSTANCE: RetrofitInstance? = null

        fun getInstance(context: Context): RetrofitInstance {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RetrofitInstance(context.applicationContext)
                    .also { INSTANCE = it }
            }
        }
    }

    // 🔹 Base URL (Always Latest)
    private fun getBaseUrl(): String {
        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return prefs.getString(Constants.BASE_URL, "") ?: ""
    }

    // 🔹 Logging
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 🔹 Token Interceptor
    private val tokenInterceptor by lazy {
        TokenInterceptor(context)
    }

    // 🔹 SSL (DEV ONLY)
    private fun getUnsafeClientBuilder(): OkHttpClient.Builder {

        val trustAllCerts = arrayOf<X509TrustManager>(
            object : X509TrustManager {
                override fun checkClientTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {}

                override fun checkServerTrusted(
                    chain: Array<java.security.cert.X509Certificate>,
                    authType: String
                ) {}

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> =
                    arrayOf()
            }
        )

        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        return OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
    }

    // 🔹 Client WITH token
    private fun authClient(): OkHttpClient {

        return getUnsafeClientBuilder()
            .addInterceptor(logging)
            .addInterceptor(tokenInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // 🔹 Client WITHOUT token (Login/Refresh)
    private fun publicClient(): OkHttpClient {

        return getUnsafeClientBuilder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    // 🔹 TGS API (No Token)
    fun tgsApi(): APLAPOLLOAPI {

        val url = getBaseUrl() + "Tgs/api/"

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(publicClient())
            .build()
            .create(APLAPOLLOAPI::class.java)
    }

    // 🔹 Service API (With Token)
    fun serviceApi(): APLAPOLLOAPI {

        val url = getBaseUrl() + "Service/api/"

        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .client(authClient())
            .build()
            .create(APLAPOLLOAPI::class.java)
    }
}


//class RetrofitInstance private constructor(private val context: Context) {
//
//    companion object {
//
//        @Volatile
//        private var INSTANCE: RetrofitInstance? = null
//
//        fun getInstance(context: Context): RetrofitInstance {
//            return INSTANCE ?: synchronized(this) {
//                INSTANCE ?: RetrofitInstance(context.applicationContext)
//                    .also { INSTANCE = it }
//            }
//        }
//    }
//
//    // 🔹 Base URL (Always Latest)
//    private fun getBaseUrl(): String {
//        val prefs = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
//        return prefs.getString(Constants.BASE_URL, "") ?: ""
//    }
//
//    // 🔹 Logging
//    private val logging = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }
//
//    // 🔹 Token Interceptor
//    private val tokenInterceptor by lazy {
//        TokenInterceptor(context)
//    }
//
//    // 🔹 SSL (DEV ONLY)
//    private fun getUnsafeClientBuilder(): OkHttpClient.Builder {
//
//        val trustAllCerts = arrayOf<X509TrustManager>(
//            object : X509TrustManager {
//                override fun checkClientTrusted(
//                    chain: Array<java.security.cert.X509Certificate>,
//                    authType: String
//                ) {}
//
//                override fun checkServerTrusted(
//                    chain: Array<java.security.cert.X509Certificate>,
//                    authType: String
//                ) {}
//
//                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> =
//                    arrayOf()
//            }
//        )
//
//        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
//        sslContext.init(null, trustAllCerts, java.security.SecureRandom())
//
//        return OkHttpClient.Builder()
//            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
//            .hostnameVerifier { _, _ -> true }
//    }
//
//    // 🔹 Client WITH token
//    private fun authClient(): OkHttpClient {
//
//        return getUnsafeClientBuilder()
//            .addInterceptor(logging)
//            .addInterceptor(tokenInterceptor)
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .build()
//    }
//
//    // 🔹 Client WITHOUT token (Login/Refresh)
//    private fun publicClient(): OkHttpClient {
//
//        return getUnsafeClientBuilder()
//            .addInterceptor(logging)
//            .connectTimeout(60, TimeUnit.SECONDS)
//            .readTimeout(60, TimeUnit.SECONDS)
//            .writeTimeout(60, TimeUnit.SECONDS)
//            .build()
//    }
//
//    // 🔹 TGS API (No Token)
//    fun tgsApi(): APLAPOLLOAPI {
//
//        val url = getBaseUrl() + "Tgs/api/"
//
//        return Retrofit.Builder()
//            .baseUrl(url)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(publicClient())
//            .build()
//            .create(APLAPOLLOAPI::class.java)
//    }
//
//    // 🔹 Service API (With Token)
//    fun serviceApi(): APLAPOLLOAPI {
//
//        val url = getBaseUrl() + "Service/api/"
//
//        return Retrofit.Builder()
//            .baseUrl(url)
//            .addConverterFactory(GsonConverterFactory.create())
//            .client(authClient())
//            .build()
//            .create(APLAPOLLOAPI::class.java)
//    }
//}
