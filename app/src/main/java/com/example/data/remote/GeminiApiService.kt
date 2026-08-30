package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

interface GeminiApiService {
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Header("x-goog-api-key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

/**
 * Hardened Gemini HTTP Client configuration.
 * - Enforces header-based authentication (`x-goog-api-key`) to prevent query-string URL leakage.
 * - Redacts sensitive authorization headers from logging.
 * - Supports runtime-provided API key injection with thread-safe atomic references.
 * - Masks sensitive keys in diagnostics and string representations.
 */
object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val HEADER_API_KEY = "x-goog-api-key"

    private val runtimeApiKey = AtomicReference<String?>(null)

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Logging interceptor with explicit redaction of all sensitive headers
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.HEADERS
        redactHeader(HEADER_API_KEY)
        redactHeader("X-Goog-Api-Key")
        redactHeader("Authorization")
        redactHeader("Proxy-Authorization")
    }

    // Security interceptor to enforce clean header-based auth and strip query param keys
    private val securityInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val originalUrl = originalRequest.url

        // Clean query parameters if "key" was accidentally passed in URL
        val sanitizedUrl = if (originalUrl.queryParameter("key") != null) {
            originalUrl.newBuilder().removeAllQueryParameters("key").build()
        } else {
            originalUrl
        }

        val requestBuilder = originalRequest.newBuilder().url(sanitizedUrl)

        // Inject header if missing and a key is available
        val existingHeader = originalRequest.header(HEADER_API_KEY)
        if (existingHeader.isNullOrBlank()) {
            val key = getApiKey()
            if (key.isNotBlank()) {
                requestBuilder.header(HEADER_API_KEY, key)
            }
        }

        chain.proceed(requestBuilder.build())
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(securityInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    val apiService: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    /**
     * Ingests a runtime-provided API key from user preferences or vault.
     */
    fun setRuntimeApiKey(key: String?) {
        runtimeApiKey.set(key?.trim())
    }

    /**
     * Clears any runtime-injected key.
     */
    fun clearApiKey() {
        runtimeApiKey.set(null)
    }

    /**
     * Retrieves the active API key safely without exposing plain-text in logs.
     */
    fun getApiKey(): String {
        val runtimeKey = runtimeApiKey.get()
        if (!runtimeKey.isNullOrBlank()) {
            return runtimeKey
        }
        return try {
            BuildConfig.GEMINI_API_KEY.trim()
        } catch (_: Throwable) {
            ""
        }
    }

    /**
     * Returns a masked preview of the configured key for UI diagnostics (e.g., "AIza...9xQ2").
     */
    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isBlank()) return "Not configured"
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    /**
     * Checks if a valid API key is present.
     */
    fun isApiKeyConfigured(): Boolean {
        return getApiKey().isNotBlank()
    }
}

