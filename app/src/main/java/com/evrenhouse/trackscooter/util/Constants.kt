package com.evrenhouse.trackscooter.util

import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.ScooterType

object StatusLabels {
    val ALL = mapOf(
        ScooterStatus.AVAILABLE to "Tersedia",
        ScooterStatus.IN_USE to "Online",
        ScooterStatus.RUSAK to "Offline / Rusak",
        ScooterStatus.MAINTENANCE to "Maintenance",
    )
    fun of(status: String?): String = ALL[status] ?: status ?: "-"
}

object StatusOrder {
    val ALL = mapOf(
        ScooterStatus.AVAILABLE to 1,
        ScooterStatus.IN_USE to 2,
        ScooterStatus.RUSAK to 3,
        ScooterStatus.MAINTENANCE to 4,
    )
}

object TypeLabels {
    val ALL = mapOf(
        ScooterType.SD to "Standar (SD)",
        ScooterType.SJ to "Jumbo (SJ)",
    )
    fun of(type: String?): String = ALL[type] ?: type ?: "-"
}

/** Device condition field definitions — order = display order. */
data class DeviceField(
    val key: String,
    val label: String,
    val options: List<Pair<String, String>>,
)

object DeviceFields {
    val ALL = listOf(
        DeviceField("setelan", "Spakbor", listOf("ada" to "Ada", "tidak" to "Tidak")),
        DeviceField("lampu", "Lampu", listOf("nyala" to "Nyala", "tidak" to "Tidak")),
        DeviceField("baterai", "Baterai", listOf("normal" to "Normal", "drop" to "Drop")),
        DeviceField("monitor", "Jenis Error", listOf("normal" to "Normal", "e2" to "E2", "e4" to "E4", "e16" to "E16", "e6" to "E6", "lain" to "Lain Lain")),
        DeviceField("rem", "Rem", listOf("normal" to "Normal", "rusak" to "Rusak")),
        DeviceField("ban", "Ban", listOf("botak" to "Botak", "tipis" to "Tipis", "aman" to "Aman")),
    )
}

object DeviceLabels {
    val setelan = mapOf("ada" to "Ada", "tidak" to "Tidak ada")
    val lampu = mapOf("nyala" to "Nyala", "tidak" to "Tidak nyala")
    val baterai = mapOf("normal" to "Normal", "drop" to "Drop")
    val monitor = mapOf("normal" to "Normal", "e2" to "E2", "e4" to "E4", "e16" to "E16", "e6" to "E6", "lain" to "Lain-lain")
    val rem = mapOf("normal" to "Normal", "rusak" to "Rusak")
    val ban = mapOf("aman" to "Aman", "tipis" to "Tipis", "botak" to "Botak")
}

object ActionLabels {
    const val CHECKOUT = "checkout"
    const val RETURN = "return"
}
