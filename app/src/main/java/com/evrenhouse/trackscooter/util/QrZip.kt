package com.evrenhouse.trackscooter.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object QrZip {

    /** Generate a PNG for each scooter, zip them, and write to Downloads. */
    suspend fun zipAllQrs(context: Context, items: List<Pair<String, String>>): String =
        withContext(Dispatchers.IO) {
            val filename = "QR-SEMUA-SCOOTER-${DateUtils.localDateKey(DateUtils.today())}.zip"

            val bytes = ByteArrayOutputStream().use { bos ->
                ZipOutputStream(bos).use { zos ->
                    items.forEach { (id, type) ->
                        val bmp = QrUtils.generate(id, 400)
                        val png = bmp.toPng()
                        zos.putNextEntry(ZipEntry("QR-$id-${type.uppercase()}.png"))
                        zos.write(png)
                        zos.closeEntry()
                    }
                }
                bos.toByteArray()
            }

            writeToDownloads(context, filename, bytes, "application/zip")
            filename
        }

    private fun Bitmap.toPng(): ByteArray {
        val baos = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

    private fun writeToDownloads(context: Context, filename: String, bytes: ByteArray, mime: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = android.content.ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mime)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Gagal membuat file di Downloads.")
            context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw IllegalStateException("Gagal menulis file.")
        } else {
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            FileOutputStream(File(dir, filename)).use { it.write(bytes) }
        }
    }
}
