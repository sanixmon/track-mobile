package com.evrenhouse.trackscooter.util

import com.evrenhouse.trackscooter.data.DeviceCondition

enum class FieldTone { NONE, BAD, WARN, GOOD }

object DeviceConditionHelper {

    private val BAD_VALUES = mapOf(
        "setelan" to "tidak",
        "lampu" to "tidak",
        "baterai" to "drop",
        "rem" to "rusak",
        "ban" to "botak",
    )

    fun isBadField(key: String, value: String?): Boolean {
        if (value.isNullOrBlank()) return false
        if (key == "monitor") return value != "normal"
        return value == BAD_VALUES[key]
    }

    fun isWarnField(key: String, value: String?): Boolean {
        return key == "ban" && value == "tipis"
    }

    /** Tone for display: NONE (never checked) | BAD | WARN | GOOD. */
    fun fieldTone(key: String, value: String?, hasCondition: Boolean): FieldTone {
        if (!hasCondition) return FieldTone.NONE
        return when {
            isBadField(key, value) -> FieldTone.BAD
            isWarnField(key, value) -> FieldTone.WARN
            else -> FieldTone.GOOD
        }
    }

    data class Issue(val text: String, val tone: FieldTone)

    /** List of bad/warn issues for card badges. */
    fun buildIssueList(condition: DeviceCondition?): List<Issue> {
        if (condition == null) return emptyList()
        val issues = mutableListOf<Issue>()
        if (condition.baterai == "drop") issues.add(Issue("Baterai drop", FieldTone.BAD))
        if (condition.lampu == "tidak") issues.add(Issue("Lampu tidak nyala", FieldTone.BAD))
        if (condition.monitor == "lain" && !condition.monitorDetail.isNullOrBlank()) {
            issues.add(Issue(condition.monitorDetail!!, FieldTone.BAD))
        } else if (condition.monitor != null && condition.monitor != "normal") {
            issues.add(Issue("Error ${condition.monitor.uppercase()}", FieldTone.BAD))
        }
        if (condition.rem == "rusak") issues.add(Issue("Rem rusak", FieldTone.BAD))
        if (condition.ban == "botak") issues.add(Issue("Ban botak", FieldTone.BAD))
        if (condition.ban == "tipis") issues.add(Issue("Ban tipis", FieldTone.WARN))
        if (condition.setelan == "tidak") issues.add(Issue("Spakbor tidak ada", FieldTone.BAD))
        return issues
    }

    /** One-line summary of condition, e.g. "Baterai drop, Ban tipis" or "Baik". */
    fun summarize(condition: DeviceCondition?): String {
        if (condition == null) return "-"
        val bad = buildIssueList(condition).map { it.text }
        return if (bad.isEmpty()) "Baik" else bad.joinToString(", ")
    }
}
