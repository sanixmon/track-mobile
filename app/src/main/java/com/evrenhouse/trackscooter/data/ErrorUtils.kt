package com.evrenhouse.trackscooter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

/** Convert a thrown exception into a short, user-facing Indonesian message. */
fun Throwable.toUserMessage(): String = when (this) {
    is TimeoutCancellationException, is SocketTimeoutException ->
        "Waktu tunggu koneksi ke server habis. Periksa sinyal internet dan coba lagi."
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
    val decoded = Json { ignoreUnknownKeys = true }.decodeFromString<ApiError>(raw)
    decoded.error?.takeIf { it.isNotBlank() } ?: decoded.message?.takeIf { it.isNotBlank() }
}.getOrNull()

/** Helper to read a raw (streamed) response body safely. */
suspend fun Response<ResponseBody>.readBytesOrNull(): ByteArray? = withContext(Dispatchers.IO) {
    runCatching { body()?.bytes() }.getOrNull()
}
