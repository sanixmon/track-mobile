package com.evrenhouse.trackscooter.util

object VersionUtils {

    /** "v2.7.11" (tag GitHub) -> "2.7.11". Kosong bila tag tak valid. */
    fun parseReleaseTag(tag: String?): String {
        if (tag.isNullOrBlank()) return ""
        return tag.trim().removePrefix("v").removePrefix("V")
    }
    /**
     * Compares two semantic version strings (e.g. "2.7.1" vs "2.7.0", or "v2.8" vs "v2.7.0").
     * Returns true if [remote] is strictly higher than [current].
     */
    fun isVersionHigher(remote: String?, current: String?): Boolean {
        if (remote.isNullOrBlank() || current.isNullOrBlank()) return false
        val rClean = remote.trim().removePrefix("v").removePrefix("V")
        val cClean = current.trim().removePrefix("v").removePrefix("V")
        val rParts = rClean.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
        val cParts = cClean.split(".").map { it.filter { ch -> ch.isDigit() }.toIntOrNull() ?: 0 }
        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
