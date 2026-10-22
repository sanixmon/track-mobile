package com.evrenhouse.trackscooter.ui.manage

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.evrenhouse.trackscooter.BuildConfig
import com.evrenhouse.trackscooter.data.Scooter
import com.evrenhouse.trackscooter.data.toUserMessage
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.LocalSweetAlert
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.UnitActionBottomSheet
import com.evrenhouse.trackscooter.ui.common.UnitCard
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Border
import com.evrenhouse.trackscooter.ui.theme.ButtonCornerRadius
import com.evrenhouse.trackscooter.ui.theme.CardCornerRadius
import com.evrenhouse.trackscooter.ui.theme.Green
import com.evrenhouse.trackscooter.ui.theme.InputCornerRadius
import com.evrenhouse.trackscooter.ui.theme.LocalThemeIsDark
import com.evrenhouse.trackscooter.ui.theme.LocalThemeToggle
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.Surface2
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import com.evrenhouse.trackscooter.ui.theme.TextPrimary
import com.evrenhouse.trackscooter.ui.theme.Warning
import com.evrenhouse.trackscooter.ui.theme.bounceClickable
import com.evrenhouse.trackscooter.util.DateUtils
import com.evrenhouse.trackscooter.util.Exporter
import com.evrenhouse.trackscooter.util.MIME_XLSX
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.QrZip
import kotlinx.coroutines.launch

/**
 * Superapp Management Hub (Tab Kelola).
 * Structured into 3 clear tonal sections:
 * 1. Inventaris Armada (Master Units + Add Unit + Export QR)
 * 2. Operasional & Data (Backup DB + Export XLSX)
 * 3. Preferensi & Sistem (Theme Switch + Auto-Update + App Info)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScreen(
    viewModel: ScooterDataViewModel,
    onOpenDetail: (String) -> Unit,
    manageViewModel: ManageViewModel = viewModel(factory = AppViewModelFactory(repository())),
) {
    val data by viewModel.state.collectAsState()
    val mv by manageViewModel.state.collectAsState()
    val activeOutlet by viewModel.selectedOutlet.collectAsState()

    val context = LocalContext.current
    val sweetAlert = LocalSweetAlert.current
    val scope = rememberCoroutineScope()
    val isDark = LocalThemeIsDark.current
    val toggleTheme = LocalThemeToggle.current

    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedScooterForAction by remember { mutableStateOf<Scooter?>(null) }
    var busyAction by remember { mutableStateOf(false) }

    LaunchedEffect(mv.toast) {
        mv.toast?.let {
            if (it.contains("berhasil", ignoreCase = true) || it.contains("sukses", ignoreCase = true)) {
                sweetAlert.showSuccess(it)
            } else {
                sweetAlert.showInfo(it)
            }
            manageViewModel.consumeToast()
        }
    }

    val todayKey = remember { DateUtils.localDateKey(DateUtils.today()) }

    // Filtered scooters by outlet & search
    val filteredScooters by remember(data.scooters, searchQuery, activeOutlet) {
        derivedStateOf {
            data.scooters.filter { s ->
                val matchesOutlet = if (searchQuery.isNotBlank()) true
                else (activeOutlet == "all" || (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == activeOutlet)
                val matchesSearch = if (searchQuery.isBlank()) true
                else s.id.contains(searchQuery.trim(), ignoreCase = true) || s.type.contains(searchQuery.trim(), ignoreCase = true)
                matchesOutlet && matchesSearch
            }
        }
    }

    val previewScooters = remember(filteredScooters, searchQuery) {
        if (searchQuery.isNotBlank()) filteredScooters.take(20) else filteredScooters.take(8)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── SEKSI 1: INVENTARIS ARMADA ──
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Inventaris Armada",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Quick Action Buttons (Tambah Unit & Export QR)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(ButtonCornerRadius),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent),
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tambah Unit", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                busyAction = true
                                runCatching {
                                    val items = data.scooters.map {
                                        Triple(it.id, it.type, it.currentOutlet ?: Outlets.getHomeOutletForType(it.type))
                                    }
                                    QrZip.zipAllQrs(context, items)
                                }
                                    .onSuccess { sweetAlert.showSuccess("Semua QR Code diunduh ($it)") }
                                    .onFailure { sweetAlert.showError(it.toUserMessage()) }
                                busyAction = false
                            }
                        },
                        enabled = data.scooters.isNotEmpty() && !busyAction,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(ButtonCornerRadius),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Border)
                        ),
                    ) {
                        Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp), tint = Accent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Unduh QR (ZIP)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Cari master unit (ID / tipe)...", fontSize = 13.sp, color = TextMuted)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
                },
                shape = RoundedCornerShape(InputCornerRadius),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = Accent,
                    unfocusedBorderColor = Border.copy(alpha = 0.7f),
                ),
                singleLine = true,
            )
        }

        items(previewScooters, key = { it.id }) { scooter ->
            UnitCard(
                scooter = scooter,
                onClick = { selectedScooterForAction = it },
            )
        }

        if (filteredScooters.size > previewScooters.size) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "+ ${filteredScooters.size - previewScooters.size} unit lainnya (gunakan pencarian untuk spesifik)",
                        fontSize = 11.sp,
                        color = TextMuted,
                    )
                }
            }
        }

        // ── SEKSI 2: OPERASIONAL & DATA ──
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Data & Operasional",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 4.dp,
                            shape = RoundedCornerShape(CardCornerRadius),
                            spotColor = Color(0x14000000),
                            ambientColor = Color(0x0A000000),
                        )
                        .clip(RoundedCornerShape(CardCornerRadius))
                        .background(Surface)
                        .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Backup Database Action
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Surface2 else Color(0xFFF8FAFC))
                                .bounceClickable {
                                    scope.launch {
                                        busyAction = true
                                        runCatching {
                                            val bytes = manageViewModel.downloadBackupBytes()
                                                ?: throw IllegalStateException("Gagal mengunduh backup.")
                                            val filename = "trackscooter_backup_${todayKey}.db"
                                            Exporter.saveBytesToDownloads(context, filename, bytes, "application/octet-stream")
                                        }
                                            .onSuccess { sweetAlert.showSuccess("Backup DB Berhasil diunduh ($it)") }
                                            .onFailure { sweetAlert.showError(it.toUserMessage()) }
                                        busyAction = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Accent.copy(alpha = if (isDark) 0.25f else 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Cadangan Basis Data (Backup)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Unduh snapshot database server (.db)", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        // Export Kondisi Unit
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) Surface2 else Color(0xFFF8FAFC))
                                .bounceClickable {
                                    scope.launch {
                                        busyAction = true
                                        runCatching {
                                            val bytes = Exporter.buildConditionsXlsx(data.scooters)
                                            val filename = "Kondisi-Scooter-${todayKey}.xlsx"
                                            Exporter.saveBytesToDownloads(context, filename, bytes, MIME_XLSX)
                                        }
                                            .onSuccess { sweetAlert.showSuccess("File XLSX berhasil diunduh ($it)") }
                                            .onFailure { sweetAlert.showError(it.toUserMessage()) }
                                        busyAction = false
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Green.copy(alpha = if (isDark) 0.25f else 0.12f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Default.GridView, contentDescription = null, tint = Green, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ekspor Kondisi Unit (.xlsx)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Laporan kesehatan fisik & komponen armada", fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        // ── SEKSI 3: PREFERENSI & SISTEM ──
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Preferensi & Sistem",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = if (isDark) 0.dp else 4.dp,
                            shape = RoundedCornerShape(CardCornerRadius),
                            spotColor = Color(0x14000000),
                            ambientColor = Color(0x0A000000),
                        )
                        .clip(RoundedCornerShape(CardCornerRadius))
                        .background(Surface)
                        .border(1.dp, Border.copy(alpha = 0.6f), RoundedCornerShape(CardCornerRadius))
                        .padding(16.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        // Theme Switch Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Warning.copy(alpha = 0.2f) else Accent.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (isDark) Warning else Accent,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Mode Tampilan", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(if (isDark) "Mode Gelap aktif" else "Mode Terang aktif", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isDark) Surface2 else Color(0xFFF1F5F9))
                                    .bounceClickable { toggleTheme() }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = if (isDark) "Ganti ke Terang" else "Ganti ke Gelap",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Warning else Accent,
                                )
                            }
                        }

                        // Auto-Update Check Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Accent.copy(alpha = if (isDark) 0.25f else 0.12f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Accent, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pembaruan Aplikasi", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text("v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Accent)
                                    .bounceClickable {
                                        viewModel.checkForAppUpdate(forceRecheck = true) { info ->
                                            if (!info.isUpdateAvailable) {
                                                sweetAlert.showSuccess("Aplikasi sudah versi terbaru (v${BuildConfig.VERSION_NAME})")
                                            }
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    text = "Cek Update",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom space for floating navigation bar clearance
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Add Scooter Dialog
    if (showAddDialog) {
        var isSubmittingAdd by remember { mutableStateOf(false) }
        AddScooterDialog(
            scooters = data.scooters,
            onDismiss = { showAddDialog = false },
            submitting = isSubmittingAdd,
            onSubmit = { id, type, outlet, addAnother ->
                scope.launch {
                    isSubmittingAdd = true
                    val success = manageViewModel.addScooter(id, type, outlet)
                    isSubmittingAdd = false
                    if (success) {
                        sweetAlert.showSuccess("Unit berhasil ditambahkan")
                        if (!addAnother) {
                            showAddDialog = false
                        }
                        viewModel.refresh()
                    } else {
                        sweetAlert.showError("Gagal menambahkan unit")
                    }
                }
            },
        )
    }

    // Unit Action Bottom Sheet
    selectedScooterForAction?.let { scooter ->
        UnitActionBottomSheet(
            scooter = scooter,
            onDismissRequest = { selectedScooterForAction = null },
            onDetailClick = { id ->
                selectedScooterForAction = null
                onOpenDetail(id)
            },
        )
    }
}
