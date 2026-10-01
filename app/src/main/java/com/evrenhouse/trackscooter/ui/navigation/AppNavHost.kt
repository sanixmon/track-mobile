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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.evrenhouse.trackscooter.ui.common.AppUpdateDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
        var hasAutoRedirected by rememberSaveable { mutableStateOf(false) }
        var switchingModeTarget by rememberSaveable { mutableStateOf<String?>(null) }
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
                        onNavigate = { item ->
                            when (item.route) {
                                Routes.MODE_MANAJEMEN -> {
                                    switchingModeTarget = ModePrefs.MANAJEMEN
                                }
                                Routes.MODE_OPERASIONAL -> {
                                    switchingModeTarget = ModePrefs.OPERASIONAL
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
        }

        // Splash screen transisi penuh layar 2 detik saat berganti mode
        switchingModeTarget?.let { targetMode ->
            ModeTransitionSplash(
                targetMode = targetMode,
                onFinished = {
                    dataViewModel.setAppMode(targetMode)
                    if (targetMode == ModePrefs.MANAJEMEN) {
                        navigateTab(Routes.MANAGE)
                    } else {
                        navigateTab(Routes.SCAN)
                    }
                    switchingModeTarget = null
                },
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
