package com.example.reclaim.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reclaim.ui.screen.DetailScreen
import com.example.reclaim.ui.screen.HomeScreen
import com.example.reclaim.ui.screen.LockScreen
import com.example.reclaim.ui.screen.SettingsScreen

@Composable
fun ReclaimNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Lock.route
    ) {
        composable(Destination.Lock.route) {
            LockScreen(
                onBack = { navController.popBackStack() },
                onGoToHabits = { navController.navigate(Destination.Home.route) },
                onTakeBreak = { /* PoC: no-op */ }
            )
        }
        composable(Destination.Home.route) {
            HomeScreen(
                onOpenDetail = { navController.navigate(Destination.Detail.route) },
                onOpenSettings = { navController.navigate(Destination.Settings.route) }
            )
        }
        composable(Destination.Detail.route) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
        composable(Destination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
