package com.evrenhouse.trackscooter.ui.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.evrenhouse.trackscooter.TrackScooterApp
import com.evrenhouse.trackscooter.data.ScooterStatus
import com.evrenhouse.trackscooter.ui.common.AppUpdateDialog
import com.evrenhouse.trackscooter.ui.common.AppViewModelFactory
import com.evrenhouse.trackscooter.ui.common.OutletBottomSheet
import com.evrenhouse.trackscooter.ui.common.ScooterDataViewModel
import com.evrenhouse.trackscooter.ui.common.SweetAlertProvider
import com.evrenhouse.trackscooter.ui.common.repository
import com.evrenhouse.trackscooter.ui.dashboard.DashboardScreen
import com.evrenhouse.trackscooter.ui.detail.ScooterDetailScreen
import com.evrenhouse.trackscooter.ui.manage.ManageScreen
import com.evrenhouse.trackscooter.ui.monitor.MonitorScreen
import com.evrenhouse.trackscooter.ui.report.ReportScreen
import com.evrenhouse.trackscooter.ui.scan.ScanScreen
import com.evrenhouse.trackscooter.ui.theme.Bg
import com.evrenhouse.trackscooter.util.Outlets
import com.evrenhouse.trackscooter.util.UpdatePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavHost() {
    SweetAlertProvider {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        val dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository()))
        val context = LocalContext.current
        val appUpdate by dataViewModel.appUpdate.collectAsState()
        val scootersDataState by dataViewModel.state.collectAsState()
        val selectedOutletId by dataViewModel.selectedOutlet.collectAsState()

        var showOutletSheet by rememberSaveable { mutableStateOf(false) }

        val monitorBadgeCount = remember(scootersDataState.scooters, selectedOutletId) {
            val inUseList = if (selectedOutletId == "all") {
                scootersDataState.scooters.filter { it.status == ScooterStatus.IN_USE }
            } else {
                scootersDataState.scooters.filter {
                    (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == selectedOutletId &&
                        it.status == ScooterStatus.IN_USE
                }
            }
            inUseList.size
        }

        val totalScootersInCurrentScope = remember(scootersDataState.scooters, selectedOutletId) {
            if (selectedOutletId == "all") {
                scootersDataState.scooters.size
            } else {
                scootersDataState.scooters.count {
                    (it.currentOutlet ?: Outlets.getHomeOutletForType(it.type)) == selectedOutletId
                }
            }
        }

        val getOutletCount = remember(scootersDataState.scooters) {
            { outletId: String ->
                if (outletId == "all") {
                    scootersDataState.scooters.size
                } else {
                    scootersDataState.scooters.count { s ->
                        (s.currentOutlet ?: Outlets.getHomeOutletForType(s.type)) == outletId
                    }
                }
            }
        }

        var hasAutoRedirected by rememberSaveable { mutableStateOf(false) }

        // Cek update tiap app dibuka dari background
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

        val isDetailScreen = currentDestination?.hierarchy?.any { it.route == Routes.DETAIL } == true
        val showChrome = !isDetailScreen

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
            containerColor = Bg,
            topBar = {
                if (showChrome) {
                    UniversalTopBar(
                        selectedOutletId = selectedOutletId,
                        totalScooterCount = totalScootersInCurrentScope,
                        onOutletClick = { showOutletSheet = true },
                    )
                }
            },
            bottomBar = {
                if (showChrome) {
                    SuperappBottomBar(
                        currentDestination = currentDestination,
                        monitorBadgeCount = monitorBadgeCount,
                        onTabSelected = { route -> navigateTab(route) },
                        onScanClick = {
                            navController.navigate(Routes.SCAN) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Bg),
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Routes.DASHBOARD,
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
                    composable(Routes.DETAIL) { entry ->
                        val id = entry.arguments?.getString("scooterId") ?: ""
                        ScooterDetailScreen(scooterId = id, onBack = { navController.popBackStack() })
                    }
                }
            }
        }

        // Large Outlet Bottom Sheet
        if (showOutletSheet) {
            OutletBottomSheet(
                selectedOutletId = selectedOutletId,
                onOutletSelected = { newOutlet ->
                    dataViewModel.setSelectedOutlet(newOutlet)
                },
                onDismissRequest = { showOutletSheet = false },
                getOutletCount = getOutletCount,
            )
        }

        // App Update Dialog
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
