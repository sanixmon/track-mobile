package com.evrenhouse.trackscooter.util

import android.content.Context

object OutletPrefs {
    private const val PREFS_NAME = "trackscooter_prefs"
    private const val KEY_SELECTED_OUTLET = "selected_outlet"

    fun getSelectedOutlet(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SELECTED_OUTLET, "all") ?: "all"
    }

    fun setSelectedOutlet(context: Context, outletId: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SELECTED_OUTLET, outletId)
            .apply()
    }
}
