package com.example.aplapollo.api

import android.content.Context
import com.example.aplapollo.helper.Constants
import com.example.aplapollo.helper.TokenInterceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
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

    // 🔹 Common OkHttpClient
    private val okHttpClient: OkHttpClient by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val tokenInterceptor = TokenInterceptor(context)

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

                override fun getAcceptedIssuers():
                        Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )

        val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, java.security.SecureRandom())

        OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0])
            .hostnameVerifier { _, _ -> true }
            .protocols(listOf(Protocol.HTTP_1_1))
            .addInterceptor(logging)
            .addInterceptor(tokenInterceptor)
            .connectTimeout(100, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.SECONDS)
            .writeTimeout(100, TimeUnit.SECONDS)
            .build()
    }

    // 🔹 Base URL from SharedPrefs
    private val baseUrl: String by lazy {
        val prefs =
            context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        prefs.getString(Constants.BASE_URL, "") ?: ""
    }

    // 🔹 TGS Retrofit (Login / Refresh)
    private val tgsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("$baseUrl/Tgs/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // 🔹 Service Retrofit (JWT protected)
    private val serviceRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("$baseUrl/Service/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    // 🔹 API providers
    fun tgsApi(): APLAPOLLOAPI =
        tgsRetrofit.create(APLAPOLLOAPI::class.java)

    fun serviceApi(): APLAPOLLOAPI =
        serviceRetrofit.create(APLAPOLLOAPI::class.java)
}
