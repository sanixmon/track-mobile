package com.evrenhouse.trackscooter.util

import androidx.compose.ui.graphics.Color
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

data class TypeColorStyle(
    val name: String,
    val accent: Color,
    val lightText: Color,
    val darkText: Color,
    val lightBg: Color,
    val darkBg: Color,
    val lightBorder: Color,
    val darkBorder: Color,
) {
    fun textColor(isDark: Boolean): Color = if (isDark) darkText else lightText
    fun bg(isDark: Boolean): Color = if (isDark) darkBg else lightBg
    fun border(isDark: Boolean): Color = if (isDark) darkBorder else lightBorder
}

object ScooterColors {
    val STYLES: Map<String, TypeColorStyle> = mapOf(
        "sd" to TypeColorStyle(
            name = "Merah Terang",
            accent = Color(0xFFEF4444),
            lightText = Color(0xFFDC2626),
            darkText = Color(0xFFF87171),
            lightBg = Color(0xFFFEF2F2),
            darkBg = Color(0x99450A0A),
            lightBorder = Color(0xFFFCA5A5),
            darkBorder = Color(0xFF7F1D1D),
        ),
        "sj" to TypeColorStyle(
            name = "Merah Tua (Ruby)",
            accent = Color(0xFF9F1239),
            lightText = Color(0xFF9F1239),
            darkText = Color(0xFFFB7185),
            lightBg = Color(0xFFFFE4E6),
            darkBg = Color(0xB34C0519),
            lightBorder = Color(0xFFFDA4AF),
            darkBorder = Color(0xFF881337),
        ),
        "sb" to TypeColorStyle(
            name = "Hijau Terang",
            accent = Color(0xFF10B981),
            lightText = Color(0xFF059669),
            darkText = Color(0xFF34D399),
            lightBg = Color(0xFFECFDF5),
            darkBg = Color(0x99022C22),
            lightBorder = Color(0xFF6EE7B7),
            darkBorder = Color(0xFF064E3B),
        ),
        "sjb" to TypeColorStyle(
            name = "Hijau Tua (Forest)",
            accent = Color(0xFF166534),
            lightText = Color(0xFF166534),
            darkText = Color(0xFF4ADE80),
            lightBg = Color(0xFFDCFCE7),
            darkBg = Color(0xB3052E16),
            lightBorder = Color(0xFF86EFAC),
            darkBorder = Color(0xFF14532D),
        ),
        "sm" to TypeColorStyle(
            name = "Biru Cerah",
            accent = Color(0xFF0EA5E9),
            lightText = Color(0xFF0284C7),
            darkText = Color(0xFF38BDF8),
            lightBg = Color(0xFFF0F9FF),
            darkBg = Color(0x99082F49),
            lightBorder = Color(0xFF7DD3FC),
            darkBorder = Color(0xFF0C4A6E),
        ),
        "sjm" to TypeColorStyle(
            name = "Biru Tua (Navy)",
            accent = Color(0xFF1E3A8A),
            lightText = Color(0xFF1E3A8A),
            darkText = Color(0xFF93C5FD),
            lightBg = Color(0xFFDBEAFE),
            darkBg = Color(0xB3172554),
            lightBorder = Color(0xFF93C5FD),
            darkBorder = Color(0xFF1E3A8A),
        ),
    )

    fun inferTypeFromId(id: String?): String? {
        if (id.isNullOrBlank()) return null
        val lower = id.trim().lowercase()
        return when {
            lower.startsWith("sjb") -> "sjb"
            lower.startsWith("sjm") -> "sjm"
            lower.startsWith("sd") -> "sd"
            lower.startsWith("sj") -> "sj"
            lower.startsWith("sb") -> "sb"
            lower.startsWith("sm") -> "sm"
            else -> null
        }
    }

    fun resolveType(type: String?, id: String? = null, outlet: String? = null): String {
        val clean = type?.trim()?.lowercase()
        if (!clean.isNullOrBlank() && STYLES.containsKey(clean)) {
            return clean
        }
        val inferred = inferTypeFromId(id)
        if (inferred != null) return inferred
        return when (outlet?.trim()?.lowercase()) {
            "utara" -> "sd"
            "barat" -> "sb"
            "utara-motor" -> "sm"
            else -> "sd"
        }
    }

    fun getStyle(type: String?, id: String? = null, outlet: String? = null): TypeColorStyle {
        val key = resolveType(type, id, outlet)
        return STYLES[key] ?: STYLES["sd"]!!
    }

    fun getScooterNameColor(
        type: String?,
        id: String? = null,
        outlet: String? = null,
        isDark: Boolean = true,
    ): Color {
        return getStyle(type, id, outlet).textColor(isDark)
    }

    fun getAccent(type: String?, id: String? = null, outlet: String? = null): Color {
        return getStyle(type, id, outlet).accent
    }

    fun getOutletColor(outlet: String?): Color {
        return when (outlet?.trim()?.lowercase()) {
            "utara" -> Color(0xFFEF4444)
            "barat" -> Color(0xFF10B981)
            "utara-motor" -> Color(0xFF0EA5E9)
            else -> Color(0xFF4F6EF7)
        }
    }
}

typealias TypeColors = ScooterColors

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
