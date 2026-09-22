package com.evrenhouse.trackscooter.util

import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.data.ScooterType

object StatusLabels {
    val ALL = mapOf(
        ScooterStatus.AVAILABLE to "Unit Ready",
        ScooterStatus.IN_USE to "Unit Diluar",
        ScooterStatus.MAINTENANCE to "Unit Kendala",
    )
    fun of(status: String?): String = ALL[status] ?: status ?: "-"
}

object StatusOrder {
    val ALL = mapOf(
        ScooterStatus.AVAILABLE to 1,
        ScooterStatus.IN_USE to 2,
        ScooterStatus.MAINTENANCE to 3,
    )
}

object TypeLabels {
    val ALL = mapOf(
        ScooterType.SD to "SD (Utara)",
        ScooterType.SJ to "SJ (Jumbo Utara)",
        ScooterType.SB to "SB (Barat)",
        ScooterType.SJB to "SJB (Jumbo Barat)",
        ScooterType.SM to "SM (Utara Motor)",
        ScooterType.SJM to "SJM (Jumbo Utara Motor)",
    )
    fun of(type: String?): String = ALL[type] ?: type?.uppercase() ?: "-"
}

data class Outlet(
    val id: String,
    val label: String,
    val shortLabel: String,
    val types: List<String>
)

object Outlets {
    val ALL_OUTLETS = listOf(
        Outlet("all", "Semua Outlet", "Semua", listOf("sd", "sj", "sb", "sjb", "sm", "sjm")),
        Outlet("utara", "Outlet Utara", "Utara", listOf("sd", "sj")),
        Outlet("barat", "Outlet Barat", "Barat", listOf("sb", "sjb")),
        Outlet("utara-motor", "Utara Motor", "Motor", listOf("sm", "sjm")),
    )
    val OPERATIONAL = ALL_OUTLETS.filter { it.id != "all" }

    val LABELS = mapOf(
        "utara" to "Outlet Utara",
        "barat" to "Outlet Barat",
        "utara-motor" to "Utara Motor",
    )

    fun labelOf(id: String?): String = LABELS[id] ?: id ?: "Outlet"

    fun getHomeOutletForType(type: String): String = when (type.lowercase()) {
        "sd", "sj" -> "utara"
        "sb", "sjb" -> "barat"
        "sm", "sjm" -> "utara-motor"
        else -> "utara"
    }
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
