package com.example.reclaim.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.eventFlow
import kotlinx.coroutines.flow.filter
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reclaim.data.Permissions
import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.App
import com.example.reclaim.reclaimApplication
import com.example.reclaim.ui.main.MainScaffold
import com.example.reclaim.ui.main.switchTab
import com.example.reclaim.ui.screen.AddAppSheet
import com.example.reclaim.ui.screen.AddHabitSheet
import com.example.reclaim.ui.screen.AppsScreen
import com.example.reclaim.ui.screen.EditAppSheet
import com.example.reclaim.ui.screen.EditHabitSheet
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
    val openOverlaySettings: () -> Unit = { Permissions.openOverlaySettings(context) }
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
            var refreshTick by remember { mutableIntStateOf(0) }
            OnResume { refreshTick++ }
            @Suppress("UNUSED_EXPRESSION") refreshTick
            OnboardingPermissionsScreen(
                usageAccessGranted = app.usageStats.hasUsageAccess(),
                overlayPermissionGranted = Permissions.canDrawOverlays(context),
                onOpenUsageAccess = openUsageAccess,
                onOpenOverlaySettings = openOverlaySettings,
                onContinue = { navController.enterMainApp() },
            )
        }

        // ---------- Main app (tab destinations) ----------
        composable(Destination.Home.route) {
            var refreshTick by remember { mutableIntStateOf(0) }
            OnResume { refreshTick++ }
            MainScaffold(
                navController = navController,
                currentTab = TabDestination.Home,
                onTabSelected = { navController.switchTab(it) }
            ) {
                HomeScreen(
                    addedApps = app.addedApps,
                    todayScreenTime = app.todayScreenTime,
                    rankAppsForHome = app.rankAppsForHome,
                    habitsRepository = app.habits,
                    habitsSummary = app.habitsTodaySummary,
                    hasUsageAccess = { app.usageStats.hasUsageAccess() },
                    hasOverlayPermission = { Permissions.canDrawOverlays(context) },
                    onOpenUsageAccess = openUsageAccess,
                    onOpenOverlaySettings = openOverlaySettings,
                    onSeeAllApps = { navController.switchTab(TabDestination.Apps) },
                    refreshTick = refreshTick,
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
                    blockingDecision = app.shouldBlockApp,
                    onAppClick = { packageName ->
                        navController.navigate(Destination.EditApp.routeFor(packageName))
                    },
                )
            }
        }
        composable(Destination.Habits.route) {
            var refreshTick by remember { mutableIntStateOf(0) }
            OnResume { refreshTick++ }
            MainScaffold(
                navController = navController,
                currentTab = TabDestination.Habits,
                onTabSelected = { navController.switchTab(it) },
                onFabClick = { navController.navigate(Destination.AddHabit.route) }
            ) {
                HabitsScreen(
                    habitsRepository = app.habits,
                    summaryUseCase = app.habitsTodaySummary,
                    onEditHabit = { id ->
                        navController.navigate(Destination.EditHabit.routeFor(id))
                    },
                    refreshTick = refreshTick,
                )
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
        composable(
            route = Destination.EditApp.route,
            arguments = listOf(navArgument(Destination.EditApp.ARG_PACKAGE_NAME) {
                type = NavType.StringType
            }),
        ) { backStackEntry ->
            val packageName = backStackEntry.arguments
                ?.getString(Destination.EditApp.ARG_PACKAGE_NAME)
                ?: return@composable
            val resolvedApp = app.appCatalog.installedApps()
                .firstOrNull { it.packageName == packageName }
                ?: App(packageName = packageName, displayName = packageName, isLauncherApp = true)
            val currentQuota = app.addedApps.addedApps()
                .firstOrNull { it.packageName == packageName }
                ?.dailyQuota
                ?: return@composable
            EditAppSheet(
                app = resolvedApp,
                initialQuota = currentQuota,
                onDismiss = { navController.popBackStack() },
                onSave = { newQuota ->
                    app.addedApps.add(AddedApp(packageName, newQuota))
                    navController.popBackStack()
                },
                onRequestRemove = {
                    app.addedApps.delete(packageName)
                    navController.popBackStack()
                },
            )
        }
        composable(
            route = Destination.EditHabit.route,
            arguments = listOf(navArgument(Destination.EditHabit.ARG_ID) {
                type = NavType.LongType
            }),
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong(Destination.EditHabit.ARG_ID)
                ?: return@composable
            val habit = app.habits.habits().firstOrNull { it.id == id }
                ?: return@composable
            EditHabitSheet(
                habit = habit,
                onDismiss = { navController.popBackStack() },
                onSave = { name, icon, reward ->
                    app.habits.update(com.example.reclaim.domain.habits.Habit(id, name, icon, reward))
                    navController.popBackStack()
                },
                onRequestRemove = {
                    app.habits.delete(id)
                    navController.popBackStack()
                },
            )
        }
        composable(Destination.AddHabit.route) {
            AddHabitSheet(
                onDismiss = { navController.popBackStack() },
                onSave = { name, icon, reward ->
                    val id = System.currentTimeMillis()
                    app.habits.add(com.example.reclaim.domain.habits.Habit(id, name, icon, reward))
                    navController.popBackStack()
                },
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

/**
 * Synchronous ON_RESUME hook. Action runs inside the LifecycleRegistry dispatch loop;
 * use only for side effects that don't mutate the NavController back stack.
 */
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

/**
 * Deferred ON_RESUME hook. Action runs on Main.immediate after the lifecycle dispatch
 * has unwound, so it's safe to navigate from inside [action].
 */
@Composable
private fun OnResumeDeferred(action: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.eventFlow
            .filter { it == Lifecycle.Event.ON_RESUME }
            .collect { action() }
    }
}
