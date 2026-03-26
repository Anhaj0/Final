package com.transitshield.app.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client for the TransitShield backend.
 */
object RetrofitClient {

    private const val BASE_URL = "http://172.20.10.3:8080/api/"

    var customBaseUrl: String? = null

    private val dynamicUrlInterceptor = okhttp3.Interceptor { chain ->
        var request = chain.request()
        val urlStr = customBaseUrl
        if (!urlStr.isNullOrEmpty()) {
            val parsed = okhttp3.HttpUrl.parse(urlStr)
            if (parsed != null) {
                val newUrl = request.url().newBuilder()
                    .scheme(parsed.scheme())
                    .host(parsed.host())
                    .port(parsed.port())
                    .build()
                request = request.newBuilder().url(newUrl).build()
            }
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    @Volatile
    var authToken: String? = null

    private val authInterceptor = okhttp3.Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
        
        authToken?.let {
            builder.header("Authorization", "Bearer ${it.trim()}")
        }
        
        val response = chain.proceed(builder.build())
        
        if (response.code == 401) {
            authToken = null
        }
        
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(dynamicUrlInterceptor)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
