package com.evrenhouse.trackscooter.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.evrenhouse.trackscooter.ui.dashboard.DashboardScreen
import com.evrenhouse.trackscooter.ui.detail.ScooterDetailScreen
import com.evrenhouse.trackscooter.ui.manage.ManageScreen
import com.evrenhouse.trackscooter.ui.monitor.MonitorScreen
import com.evrenhouse.trackscooter.ui.report.ReportScreen
import com.evrenhouse.trackscooter.ui.scan.ScanScreen

private val bottomItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Routes.MONITOR, "Monitor", Icons.Filled.Monitor),
    BottomNavItem(Routes.SCAN, "Scan", Icons.Filled.QrCodeScanner),
    BottomNavItem(Routes.REPORT, "Laporan", Icons.Filled.Assignment),
    BottomNavItem(Routes.MANAGE, "Kelola", Icons.Filled.Inventory2),
)

@Composable
fun AppNavHost() {
    SweetAlertProvider {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = backStackEntry?.destination

        // One shared data source across Dashboard / Monitor / Manage (single 30s poll)
        val dataViewModel: ScooterDataViewModel = viewModel(factory = AppViewModelFactory(repository()))

        // Hide bottom bar on the detail screen (full-screen modal-like page)
        val showBottomBar = bottomItems.any { item ->
            currentDestination?.hierarchy?.any { it.route == item.route } == true
        }

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                FloatingBottomBar(
                    items = bottomItems,
                    currentDestination = currentDestination,
                    onNavigate = { item ->
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.DASHBOARD,
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
                    )
                }
                composable(Routes.SCAN) {
                    ScanScreen()
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
}
}
