package com.evrenhouse.trackscooter.util

import android.content.Context

/**
 * Persistensi status notifikasi update agar user versi lama tetap diingatkan:
 * - versi update yang pernah di-dismiss + waktunya (ingatkan lagi setelah [UpdatePolicy.REMIND_INTERVAL_MS])
 * - versi update yang sudah pernah auto-redirect ke browser (redirect sekali per versi)
 */
object UpdatePrefs {
    private const val PREFS_NAME = "trackscooter_prefs"
    private const val KEY_DISMISSED_VERSION_CODE = "update_dismissed_version_code"
    private const val KEY_DISMISSED_AT = "update_dismissed_at"
    private const val KEY_REDIRECTED_VERSION_CODE = "update_redirected_version_code"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getDismissedVersionCode(context: Context): Int =
        prefs(context).getInt(KEY_DISMISSED_VERSION_CODE, 0)

    fun getDismissedAt(context: Context): Long =
        prefs(context).getLong(KEY_DISMISSED_AT, 0L)

    fun setDismissed(context: Context, versionCode: Int, atMillis: Long) {
        prefs(context).edit()
            .putInt(KEY_DISMISSED_VERSION_CODE, versionCode)
            .putLong(KEY_DISMISSED_AT, atMillis)
            .apply()
    }

    fun getRedirectedVersionCode(context: Context): Int =
        prefs(context).getInt(KEY_REDIRECTED_VERSION_CODE, 0)

    fun setRedirected(context: Context, versionCode: Int) {
        prefs(context).edit()
            .putInt(KEY_REDIRECTED_VERSION_CODE, versionCode)
            .apply()
    }
}
