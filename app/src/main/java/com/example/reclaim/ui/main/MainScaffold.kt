package com.example.reclaim.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.reclaim.navigation.Destination
import com.example.reclaim.navigation.TabDestination
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTheme

/**
 * Wraps a tab destination's content with the persistent bottom tab bar
 * (and the floating + button on Apps and Habits).
 */
@Composable
fun MainScaffold(
    navController: NavHostController,
    currentTab: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    onFabClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(ReclaimBg)) {
        // Reserve space at the bottom for the tab bar so screen content doesn't slide under it.
        Box(modifier = Modifier.fillMaxSize().padding(bottom = TAB_BAR_HEIGHT)) {
            content()
        }
        if (onFabClick != null) {
            FloatingPlusButton(
                onClick = onFabClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = TAB_BAR_HEIGHT + 16.dp)
            )
        }
        BottomTabBar(
            current = currentTab,
            onTabSelected = onTabSelected,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/**
 * Reads the current route from the [navController] and resolves it to a [TabDestination].
 * Returns null when the active route is not a tab destination (e.g. onboarding, lock).
 */
@Composable
fun NavController.currentTabDestination(): TabDestination? {
    val backStackEntry by currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route ?: return null
    return TabDestination.entries.firstOrNull { it.destination.route == route }
}

@Composable
private fun BottomTabBar(
    current: TabDestination,
    onTabSelected: (TabDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ReclaimBg)
            .border(1.dp, ReclaimLine)
            .height(TAB_BAR_HEIGHT)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TabDestination.entries.forEach { tab ->
            TabItem(
                tab = tab,
                selected = tab == current,
                onClick = { onTabSelected(tab) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabItem(
    tab: TabDestination,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = if (selected) ReclaimTeal else ReclaimInk3
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = tab.icon(),
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = tab.label,
            color = tint,
            fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
        )
    }
}

private fun TabDestination.icon(): ImageVector = when (this) {
    TabDestination.Home -> Icons.Filled.Home
    TabDestination.Apps -> Icons.Filled.Apps
    TabDestination.Habits -> Icons.Filled.GpsFixed
}

@Composable
private fun FloatingPlusButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(ReclaimTeal)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "Add",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}

private val TAB_BAR_HEIGHT = 64.dp

/**
 * Switch active tab without piling up entries on the back stack — Material's
 * standard bottom-nav pattern.
 */
fun NavController.switchTab(tab: TabDestination) {
    navigate(tab.destination.route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Preview
@Composable
private fun MainScaffoldPreview() {
    ReclaimTheme {
        MainScaffold(
            navController = rememberNavController(),
            currentTab = TabDestination.Apps,
            onTabSelected = {},
            onFabClick = {}
        ) {
            Box(Modifier.fillMaxSize().background(ReclaimBg)) {
                Text("Tab content", modifier = Modifier.padding(24.dp), color = ReclaimInk)
            }
        }
    }
}
