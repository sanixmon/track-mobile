package com.evrenhouse.trackscooter.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val MONITOR = "monitor"
    const val SCAN = "scan"
    const val REPORT = "report"
    const val MANAGE = "manage"
    const val MANAGE_DASHBOARD = "manage_dashboard"
    const val DETAIL = "detail/{scooterId}"

    /** Sentinel aksi pindah ranah (bukan destinasi nav): ditangani di AppNavHost. */
    const val MODE_MANAJEMEN = "__mode_manajemen"
    const val MODE_OPERASIONAL = "__mode_operasional"

    fun detail(scooterId: String) = "detail/${java.net.URLEncoder.encode(scooterId, "UTF-8")}"
}
