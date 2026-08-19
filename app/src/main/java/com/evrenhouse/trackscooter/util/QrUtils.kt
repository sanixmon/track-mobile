package com.evrenhouse.trackscooter.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object QrUtils {

    private const val DARK = 0xFF0D1017.toInt()
    private const val LIGHT = 0xFFFFFFFF.toInt()

    /** Generate a QR bitmap for the given text (scooter id), high error correction. */
    suspend fun generate(text: String, sizePx: Int = 600): Bitmap = withContext(Dispatchers.Default) {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to 2,
        )
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        for (x in 0 until sizePx) {
            for (y in 0 until sizePx) {
                bmp.setPixel(x, y, if (matrix.get(x, y)) DARK else LIGHT)
            }
        }
        bmp
    }
}
