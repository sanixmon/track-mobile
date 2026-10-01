package com.evrenhouse.trackscooter.data

import com.evrenhouse.trackscooter.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.Interceptor
import okhttp3.Response

object ApiClient {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val okHttp: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(RetryOn5xxInterceptor())

        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    val baseUrl: String get() = BuildConfig.API_BASE_URL.trimEnd('/')

    val sseClient: OkHttpClient by lazy {
        okHttp.newBuilder()
            .readTimeout(0, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val service: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    /** Client repo rilis khusus (fallback cek update bila API utama gagal). */
    val githubService: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(okHttp)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApiService::class.java)
    }
}

/** Interceptor to retry on transient upstream 502/503/504 errors with exponential backoff */
private class RetryOn5xxInterceptor(private val maxRetries: Int = 2) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response = chain.proceed(request)
        var attempt = 0

        while (!response.isSuccessful && response.code in 502..504 && attempt < maxRetries) {
            attempt++
            response.close()
            val backoffMs = (350L * attempt).coerceAtMost(1200L)
            try {
                Thread.sleep(backoffMs)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                break
            }
            response = chain.proceed(request)
        }
        return response
    }
}
