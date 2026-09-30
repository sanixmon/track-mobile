package com.evrenhouse.trackscooter.util

import android.content.Context

/**
 * Ranah aplikasi ala web (LayoutPreview operasional vs ManajemenLayout):
 * - OPERASIONAL (default): Scan, Monitor, Laporan, Dashboard. Petugas
 *   lapangan mendarat di Scan dan tidak melihat aksi admin.
 * - MANAJEMEN: Kelola (+ Dashboard) + jalan kembali ke Operasional.
 */
object ModePrefs {
    const val OPERASIONAL = "operasional"
    const val MANAJEMEN = "manajemen"

    private const val PREFS_NAME = "trackscooter_prefs"
    private const val KEY_APP_MODE = "app_mode"

    fun getAppMode(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_APP_MODE, OPERASIONAL)
            .takeIf { it == MANAJEMEN || it == OPERASIONAL }
            ?: OPERASIONAL
    }

    fun setAppMode(context: Context, mode: String) {
        val clean = if (mode == MANAJEMEN) MANAJEMEN else OPERASIONAL
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_APP_MODE, clean)
            .apply()
    }
}
