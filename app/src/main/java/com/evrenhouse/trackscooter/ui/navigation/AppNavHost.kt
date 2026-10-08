package com.evrenhouse.trackscooter.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.evrenhouse.trackscooter.ui.common.AppUpdateDialog
import com.evrenhouse.trackscooter.data.ScooterStatus
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.evrenhouse.trackscooter.ui.common.UnifiedOutletPicker
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.evrenhouse.trackscooter.ui.theme.Accent
import com.evrenhouse.trackscooter.ui.theme.Surface
import com.evrenhouse.trackscooter.ui.theme.TextMuted
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.SweetAlertProvider
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.TrackScooterApp
import com.evrenhouse.trackscooter.util.ModePrefs
import com.evrenhouse.trackscooter.util.UpdatePrefs
import com.evrenhouse.trackscooter.ui.dashboard.DashboardScreen
import com.evrenhouse.trackscooter.ui.detail.ScooterDetailScreen
import com.evrenhouse.trackscooter.ui.manage.ManageDashboardScreen
import com.evrenhouse.trackscooter.ui.manage.ManageScreen
import com.evrenhouse.trackscooter.ui.monitor.MonitorScreen
import com.evrenhouse.trackscooter.ui.report.ReportScreen
import com.evrenhouse.trackscooter.ui.scan.ScanScreen

// Ranah ala web: Operasional (Scan sebagai beranda, tanpa aksi admin)
// vs Manajemen (Kelola). Item terakhir tiap bar adalah aksi pindah ranah.
private val operasionalItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Routes.MONITOR, "Monitor", Icons.Filled.Monitor),
    BottomNavItem(Routes.SCAN, "Scan", Icons.Filled.QrCodeScanner),
    BottomNavItem(Routes.REPORT, "Laporan", Icons.Filled.Assignment),
    BottomNavItem(Routes.MODE_MANAJEMEN, "Kelola", Icons.Outlined.Inventory2),
)

private val manajemenItems = listOf(
    BottomNavItem(Routes.MANAGE, "Kelola", Icons.Outlined.Inventory2),
    BottomNavItem(Routes.MANAGE_DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Routes.MODE_OPERASIONAL, "Operasional", Icons.Filled.Home),
)

@Composable
fun AppNavHost() {
    SweetAlertProvider {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination
        // One shared data source across Dashboard / Monitor / Manage (single 30s poll)
        val dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository()))
        val context = LocalContext.current
        val appUpdate by dataViewModel.appUpdate.collectAsState()
        val appMode by dataViewModel.appMode.collectAsState()
        val bottomItems = if (appMode == ModePrefs.MANAJEMEN) manajemenItems else operasionalItems
        val scootersDataState by dataViewModel.state.collectAsState()
        val selectedOutletId by dataViewModel.selectedOutlet.collectAsState()
        val monitorBadgeCount = remember(scootersDataState.scooters, selectedOutletId) {
            val inUseList = if (selectedOutletId == "all") {
                scootersDataState.scooters.filter { it.status == ScooterStatus.IN_USE }
            } else {
                scootersDataState.scooters.filter {
                    (it.currentOutlet ?: com.evrenhouse.trackscooter.util.Outlets.getHomeOutletForType(it.type)) == selectedOutletId &&
                        it.status == ScooterStatus.IN_USE
                }
            }
            inUseList.size
        }
        var hasAutoRedirected by rememberSaveable { mutableStateOf(false) }
        var switchingModeTarget by rememberSaveable { mutableStateOf<String?>(null) }
        var pendingModeSwitch by rememberSaveable { mutableStateOf<String?>(null) }
        val initialMode = rememberSaveable { dataViewModel.appMode.value }
        val startDestination = if (initialMode == ModePrefs.MANAJEMEN) Routes.MANAGE else Routes.SCAN

        // Cek update tiap app dibuka dari background: cek di init tidak jalan
        // ulang bila proses hidup terus (kasus "app lama tidak dapat notif").
        // Throttle 15 menit di dalam checkForAppUpdate.
        val lifecycleOwner = LocalLifecycleOwner.current
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    dataViewModel.checkForAppUpdate()
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        LaunchedEffect(appUpdate) {
            val update = appUpdate
            if (update != null && update.isUpdateAvailable && !hasAutoRedirected) {
                hasAutoRedirected = true
                // Redirect otomatis sekali per versi update (persist antar restart),
                // agar user versi lama pasti diarahkan ke unduhan minimal satu kali.
                val alreadyRedirected = runCatching {
                    UpdatePrefs.getRedirectedVersionCode(TrackScooterApp.instance) >= update.latestVersionCode
                }.getOrDefault(false)
                if (!alreadyRedirected) {
                    runCatching {
                        UpdatePrefs.setRedirected(TrackScooterApp.instance, update.latestVersionCode)
                    }
                    val url = update.downloadUrl.takeIf { it.isNotBlank() }
                        ?: "https://github.com/sanixmon/track-releases/releases/latest/download/track-scooter.apk"
                    runCatching {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }

        // Bottom bar ditampilkan pada semua layar navigasi utama,
        // dan disembunyikan hanya saat di layar detail unit atau saat splash mode transition aktif.
        val isDetailScreen = currentDestination?.hierarchy?.any { it.route == Routes.DETAIL } == true
        val showBottomBar = !isDetailScreen && switchingModeTarget == null

        // Sinkronisasi otomatis mode saat mendarat di rute mode spesifik
        LaunchedEffect(currentDestination?.route) {
            val route = currentDestination?.route ?: return@LaunchedEffect
            if (route in listOf(Routes.MANAGE, Routes.MANAGE_DASHBOARD) && appMode != ModePrefs.MANAJEMEN) {
                dataViewModel.setAppMode(ModePrefs.MANAJEMEN)
            } else if (route in listOf(Routes.DASHBOARD, Routes.MONITOR, Routes.SCAN, Routes.REPORT) && appMode != ModePrefs.OPERASIONAL) {
                dataViewModel.setAppMode(ModePrefs.OPERASIONAL)
            }
        }

        fun navigateTab(route: String) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                // Transisi smooth saat daftar tab berganti antar mode.
                Crossfade(
                    targetState = appMode,
                    animationSpec = tween(250),
                    label = "nav_mode",
                ) {
                    FloatingBottomBar(
                        items = bottomItems,
                        currentDestination = currentDestination,
                        monitorBadgeCount = monitorBadgeCount,
                        onNavigate = { item ->
                            when (item.route) {
                                Routes.MODE_MANAJEMEN -> {
                                    pendingModeSwitch = ModePrefs.MANAJEMEN
                                }
                                Routes.MODE_OPERASIONAL -> {
                                    pendingModeSwitch = ModePrefs.OPERASIONAL
                                }
                                else -> navigateTab(item.route)
                            }
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            // Transisi geser + fade antar layar (termasuk pindah ranah).
            NavHost(
                navController = navController,
                startDestination = startDestination,
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it / 4 },
                        animationSpec = tween(220),
                    ) + fadeIn(animationSpec = tween(220))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },
                        animationSpec = tween(220),
                    ) + fadeOut(animationSpec = tween(220))
                },
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it / 4 },
                        animationSpec = tween(220),
                    ) + fadeIn(animationSpec = tween(220))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = tween(220),
                    ) + fadeOut(animationSpec = tween(220))
                },
            ) {
                composable(Routes.DASHBOARD) {
                    DashboardScreen(
                        viewModel = dataViewModel,
                        onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                        onGoScan = { navController.navigate(Routes.SCAN) },
                    )
                }
                composable(Routes.MONITOR) {
                    MonitorScreen(
                        viewModel = dataViewModel,
                        onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                    )
                }
                composable(Routes.SCAN) {
                    ScanScreen(dataViewModel = dataViewModel)
                }
                composable(Routes.REPORT) {
                    ReportScreen(
                        viewModel = dataViewModel,
                        onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                    )
                }
                composable(Routes.MANAGE) {
                    ManageScreen(
                        viewModel = dataViewModel,
                        onOpenDetail = { id -> navController.navigate(Routes.detail(id)) },
                    )
                }
                composable(Routes.MANAGE_DASHBOARD) {
                    ManageDashboardScreen(viewModel = dataViewModel)
                }
                composable(Routes.DETAIL) { entry ->
                    val id = entry.arguments?.getString("scooterId") ?: ""
                    ScooterDetailScreen(scooterId = id, onBack = { navController.popBackStack() })
                }
            }

            // ── Floating Unified Outlet Picker (1:1 with Web UnifiedOutletPicker.jsx) ──
            if (showBottomBar) {
                UnifiedOutletPicker(
                    viewModel = dataViewModel,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 12.dp),
                )
            }
        }
        // Splash screen transisi penuh layar 2 detik saat berganti mode
        switchingModeTarget?.let { targetMode ->
            ModeTransitionSplash(
                targetMode = targetMode,
                onSwitch = {
                    dataViewModel.setAppMode(targetMode)
                    if (targetMode == ModePrefs.MANAJEMEN) {
                        navigateTab(Routes.MANAGE)
                    } else {
                        navigateTab(Routes.SCAN)
                    }
                },
                onFinished = {
                    switchingModeTarget = null
                },
            )
        }

        // Dialog Konfirmasi sebelum Beralih Mode Operasional <-> Manajemen
        pendingModeSwitch?.let { targetMode ->
            val isToManajemen = targetMode == ModePrefs.MANAJEMEN
            AlertDialog(
                onDismissRequest = { pendingModeSwitch = null },
                title = {
                    Text(
                        text = if (isToManajemen) "Beralih ke Mode Manajemen?" else "Beralih ke Mode Operasional?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                    )
                },
                text = {
                    Text(
                        text = if (isToManajemen) {
                            "Mode Manajemen digunakan untuk administrasi inventaris, kelola unit, dan data analitik lanjutan."
                        } else {
                            "Mode Operasional digunakan untuk aktivitas lapangan: pindai QR sewa, monitor armada, dan closing harian."
                        },
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val target = pendingModeSwitch
                            pendingModeSwitch = null
                            if (target != null) {
                                switchingModeTarget = target
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("Ya, Beralih Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { pendingModeSwitch = null },
                        shape = RoundedCornerShape(8.dp),
                    ) {
                        Text("Batal", fontSize = 13.sp)
                    }
                },
                containerColor = Surface,
                shape = RoundedCornerShape(16.dp),
            )
        }
    }
        appUpdate?.let { update ->
            if (update.isUpdateAvailable) {
                AppUpdateDialog(
                    updateInfo = update,
                    onDownload = {
                        val url = update.downloadUrl.takeIf { it.isNotBlank() }
                            ?: "https://github.com/sanixmon/track-releases/releases/latest/download/track-scooter.apk"
                        runCatching {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    },
                    onDismiss = {
                        dataViewModel.dismissUpdateDialog()
                    }
                )
            }
        }
    }
}
