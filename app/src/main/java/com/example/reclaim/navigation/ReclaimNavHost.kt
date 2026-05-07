package com.example.reclaim.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.reclaim.reclaimApplication
import com.example.reclaim.ui.main.MainScaffold
import com.example.reclaim.ui.main.switchTab
import com.example.reclaim.ui.screen.AddAppSheet
import com.example.reclaim.ui.screen.AddHabitSheet
import com.example.reclaim.ui.screen.AppsScreen
import com.example.reclaim.ui.screen.HabitsScreen
import com.example.reclaim.ui.screen.HomeScreen
import com.example.reclaim.ui.screen.LockScreen
import com.example.reclaim.ui.screen.OnboardingPermissionsScreen
import com.example.reclaim.ui.screen.OnboardingValueScreen

@Composable
fun ReclaimNavHost(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.reclaimApplication()
    val openUsageAccess: () -> Unit = {
        context.startActivity(
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
    NavHost(
        navController = navController,
        startDestination = Destination.OnboardingValue.route
    ) {
        // ---------- Onboarding ----------
        composable(Destination.OnboardingValue.route) {
            OnboardingValueScreen(
                onContinue = { navController.navigate(Destination.OnboardingPermissions.route) }
            )
        }
        composable(Destination.OnboardingPermissions.route) {
            OnResume {
                if (app.usageStats.hasUsageAccess()) navController.enterMainApp()
            }
            OnboardingPermissionsScreen(
                onOpenUsageAccess = openUsageAccess,
                onSkip = { navController.enterMainApp() },
            )
        }

        // ---------- Main app (tab destinations) ----------
        composable(Destination.Home.route) {
            MainScaffold(
                navController = navController,
                currentTab = TabDestination.Home,
                onTabSelected = { navController.switchTab(it) }
            ) {
                HomeScreen(
                    onSeeAllApps = { navController.switchTab(TabDestination.Apps) }
                )
            }
        }
        composable(Destination.Apps.route) {
            MainScaffold(
                navController = navController,
                currentTab = TabDestination.Apps,
                onTabSelected = { navController.switchTab(it) },
                onFabClick = { navController.navigate(Destination.AddApp.route) }
            ) {
                AppsScreen(
                    addedApps = app.addedApps,
                    catalog = app.appCatalog,
                    onAppClick = { _ -> navController.navigate(Destination.Lock.route) },
                )
            }
        }
        composable(Destination.Habits.route) {
            MainScaffold(
                navController = navController,
                currentTab = TabDestination.Habits,
                onTabSelected = { navController.switchTab(it) },
                onFabClick = { navController.navigate(Destination.AddHabit.route) }
            ) {
                HabitsScreen()
            }
        }

        // ---------- Modals (rendered as separate destinations; the bottom sheet
        // dismisses by popping back to the previous destination) ----------
        composable(Destination.AddApp.route) {
            OnResume { app.appCatalog.invalidate() }
            AddAppSheet(
                suggestApps = app.suggestApps,
                searchApps = app.searchApps,
                addedApps = app.addedApps,
                hasUsageAccess = { app.usageStats.hasUsageAccess() },
                onOpenUsageAccess = openUsageAccess,
                onDismiss = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(Destination.AddHabit.route) {
            AddHabitSheet(
                onDismiss = { navController.popBackStack() },
                onSave = { navController.popBackStack() }
            )
        }

        // ---------- Lock screen ----------
        composable(Destination.Lock.route) {
            LockScreen(
                onBack = { navController.popBackStack() },
                onGoToHabits = {
                    navController.popBackStack()
                    navController.switchTab(TabDestination.Habits)
                },
                onTakeBreak = { navController.popBackStack() }
            )
        }
    }
}

/**
 * Onboarding → main app: clear the onboarding back stack so back from Home exits the app.
 */
private fun NavHostController.enterMainApp() {
    navigate(Destination.Home.route) {
        popUpTo(graph.findStartDestination().id) { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
private fun OnResume(action: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) action()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
