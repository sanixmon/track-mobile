package com.evrenhouse.trackscooter.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrUtils {
    const val COLOR_UTARA = 0xFF000000.toInt()       // Hitam
    const val COLOR_UTARA_MOTOR = 0xFF0284C7.toInt() // Biru langit
    const val COLOR_BARAT = 0xFF15803D.toInt()       // Hijau sedikit tua
    const val COLOR_DEFAULT = 0xFF000000.toInt()
    const val LIGHT = 0xFFFFFFFF.toInt()
    fun getQrColorForOutlet(outlet: String?): Int {
        return when (outlet?.lowercase()) {
            "utara" -> COLOR_UTARA
            "utara-motor" -> COLOR_UTARA_MOTOR
            "barat" -> COLOR_BARAT
            else -> COLOR_DEFAULT
        }
    }

    /** Generate a QR bitmap with outlet color and the scooter number printed small below. */
    suspend fun generate(
        scooterId: String,
        outlet: String? = null,
        sizePx: Int = 600
    ): Bitmap = withContext(Dispatchers.Default) {
        val darkColor = getQrColorForOutlet(outlet)
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 1,
        )
        val matrix = QRCodeWriter().encode(scooterId, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val numberOnly = scooterId.substringAfter("-").ifBlank { scooterId.filter { it.isDigit() } }
        val extraHeight = (sizePx * 0.30f).toInt()
        val totalHeight = sizePx + extraHeight

        val bmp = Bitmap.createBitmap(sizePx, totalHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        canvas.drawColor(LIGHT)

        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                if (matrix.get(x, y)) {
                    bmp.setPixel(x, y, darkColor)
                }
            }
        }

        if (numberOnly.isNotBlank()) {
            val paint = Paint().apply {
                color = darkColor
                textSize = sizePx * 0.38f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            }
            val xPos = sizePx / 2f
            val yPos = sizePx + (extraHeight * 0.80f)
            canvas.drawText(numberOnly, xPos, yPos, paint)
        }

        bmp
    }

    /** Overload for backward compatibility */
    suspend fun generate(text: String, sizePx: Int = 600): Bitmap =
        generate(scooterId = text, outlet = null, sizePx = sizePx)
}

/** Convert an Android Bitmap to raw PNG bytes. */
fun Bitmap.toPngBytes(): ByteArray {
    val baos = java.io.ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, baos)
    return baos.toByteArray()
}

