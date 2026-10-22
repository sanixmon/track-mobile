package com.evrenhouse.trackscooter.ui.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val MONITOR = "monitor"
    const val SCAN = "scan"
    const val REPORT = "report"
    const val MANAGE = "manage"
    const val DETAIL = "detail/{scooterId}"

    fun detail(scooterId: String) = "detail/${java.net.URLEncoder.encode(scooterId, "UTF-8")}"
}
