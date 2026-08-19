package com.evrenhouse.trackscooter.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object Exporter {

    private fun escapeCsv(value: Any?): String {
        val s = value?.toString() ?: "-"
        return if (s.contains(',') || s.contains('"') || s.contains('\n')) {
            "\"" + s.replace("\"", "\"\"") + "\""
        } else {
            s
        }
    }

    fun toCsv(header: List<String>, rows: List<List<Any?>>): String = buildString {
        append(header.joinToString(",") { escapeCsv(it) })
        append("\n")
        rows.forEach { row ->
            append(row.joinToString(",") { escapeCsv(it) })
            append("\n")
        }
    }

    /** Write a text file into public Downloads; returns the display filename. */
    suspend fun saveToDownloads(context: Context, filename: String, content: String): String =
        saveBytesToDownloads(context, filename, content.toByteArray(), "text/csv")

    /** Write raw bytes (e.g. PNG, ZIP, .db) into public Downloads. */
    suspend fun saveBytesToDownloads(context: Context, filename: String, bytes: ByteArray, mime: String = "application/octet-stream"): String =
        withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mime)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: throw IllegalStateException("Gagal membuat file di Downloads.")
                resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    ?: throw IllegalStateException("Gagal menulis file.")
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { it.write(bytes) }
            }
            filename
        }
}
