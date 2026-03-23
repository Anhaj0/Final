package com.transitshield.app.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the TransitShield backend.
 *
 * TransitShield backend base URL for physical device testing.
 */
object RetrofitClient {

    const val BASE_URL = "http://192.168.8.196:8080/api/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    var authToken: String? = null

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()

        authToken?.takeIf { it.isNotBlank() }?.let {
            builder.header("Authorization", "Bearer $it")
        }

        chain.proceed(builder.build())
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
