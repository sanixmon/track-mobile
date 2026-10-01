package com.evrenhouse.trackscooter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/** Convert a thrown exception into a short, user-facing Indonesian message. */
fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Tidak dapat terhubung ke server API. Periksa koneksi internet."
    is HttpException -> {
        when (code()) {
            502, 503, 504 -> "Server backend sedang sibuk atau dalam pemeliharaan (${code()}). Silakan coba beberapa saat lagi."
            else -> {
                val body = errorBodyMessage()
                if (!body.isNullOrBlank()) body else "Permintaan gagal (${code()})."
            }
        }
    }
    else -> message ?: "Terjadi kesalahan. Silakan coba lagi."
}

fun HttpException.errorBodyMessage(): String? = runCatching {
    val raw = response()?.errorBody()?.string() ?: return null
    Json { ignoreUnknownKeys = true }.decodeFromString<ApiError>(raw).error
}.getOrNull()

/** Helper to read a raw (streamed) response body safely. */
suspend fun Response<ResponseBody>.readBytesOrNull(): ByteArray? = withContext(Dispatchers.IO) {
    runCatching { body()?.bytes() }.getOrNull()
}
