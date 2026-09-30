package com.evrenhouse.trackscooter.util

/**
 * Kebijakan murni (pure, testable) untuk notifikasi update — tanpa dependensi Android.
 *
 * Masalah yang diperbaiki: user versi lama menekan "Nanti Saja" sekali lalu
 * tidak pernah diingatkan lagi, dan [minVersionCode] dari server diabaikan.
 */
object UpdatePolicy {

    /** Cek ulang update ke server tiap 6 jam (polling data utama tetap 30 detik). */
    const val RECHECK_INTERVAL_MS = 6L * 60L * 60L * 1000L

    /** Setelah dismiss, ingatkan lagi maksimal sekali sehari sampai user update. */
    const val REMIND_INTERVAL_MS = 24L * 60L * 60L * 1000L

    /**
     * Force update jika server eksplisit memaksa ATAU versi user di bawah
     * batas minimum yang didukung ([minVersionCode] <= 0 berarti tidak ada batas).
     */
    fun isForceUpdate(currentCode: Int, minVersionCode: Int, forceFlag: Boolean): Boolean {
        if (forceFlag) return true
        return minVersionCode > 0 && currentCode < minVersionCode
    }

    /**
     * Tentukan apakah dialog update harus ditampilkan.
     * - Force update: selalu tampil (tidak bisa di-dismiss permanen).
     * - Soft update: tampil jika belum pernah di-dismiss, atau versi terbaru
     *   berbeda dari yang di-dismiss, atau sudah lewat [remindIntervalMs].
     */
    fun shouldShowUpdate(
        forceUpdate: Boolean,
        latestCode: Int,
        dismissedCode: Int,
        dismissedAt: Long,
        nowMillis: Long,
        remindIntervalMs: Long = REMIND_INTERVAL_MS,
    ): Boolean {
        if (forceUpdate) return true
        if (dismissedCode <= 0) return true
        if (dismissedCode != latestCode) return true
        if (dismissedAt <= 0L) return true
        return nowMillis - dismissedAt >= remindIntervalMs
    }
}
