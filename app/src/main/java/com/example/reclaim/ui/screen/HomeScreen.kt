package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.TodayScreenTimeUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration

@Composable
fun HomeScreen(
    addedApps: AddedAppsRepository,
    todayScreenTime: TodayScreenTimeUseCase,
    hasUsageAccess: () -> Boolean,
    onOpenUsageAccess: () -> Unit,
    refreshTick: Int = 0,
) {
    @Suppress("UNUSED_EXPRESSION") refreshTick
    val added = addedApps.addedApps()
    val limit = added.fold(Duration.ZERO) { acc, app -> acc + app.dailyQuota }
    val today = todayScreenTime.invoke()
    val accessGranted = hasUsageAccess()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .padding(24.dp),
    ) {
        HomeScreenContent(
            todayScreenTime = today,
            dailyLimit = limit,
            hasUsageAccess = accessGranted,
            hasAddedApps = added.isNotEmpty(),
            onOpenUsageAccess = onOpenUsageAccess,
        )
    }
}

private fun formatScreenTime(d: Duration): String {
    if (d == Duration.ZERO) return "0h 0m"
    val h = d.inWholeHours
    val m = (d.inWholeMinutes % 60).toInt()
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}

private val HEADER_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.ENGLISH)

@Composable
internal fun HomeScreenContent(
    todayScreenTime: Duration,
    dailyLimit: Duration,
    hasUsageAccess: Boolean,
    hasAddedApps: Boolean,
    onOpenUsageAccess: () -> Unit,
    today: LocalDate = LocalDate.now(),
) {
    val exceeded = hasUsageAccess && todayScreenTime > dailyLimit
    Column {
        Text(text = today.format(HEADER_DATE_FORMATTER).uppercase(Locale.ENGLISH))
        Text(text = "Good afternoon")
        Text(
            text = if (hasUsageAccess) formatScreenTime(todayScreenTime) else "—",
            color = if (exceeded) ReclaimRed else ReclaimInk,
            modifier = Modifier.semantics {
                contentDescription = if (exceeded) "Screen time today, exceeded" else "Screen time today"
            },
        )
        if (hasAddedApps) {
            Text(text = "of your ${formatScreenTime(dailyLimit)} daily limit")
        } else {
            Text(text = "Add apps to set your daily limit")
        }
        if (!hasUsageAccess) {
            TextButton(onClick = onOpenUsageAccess) {
                Text(text = "Grant usage access")
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    ReclaimTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ReclaimBg)
                .padding(24.dp),
        ) {
            HomeScreenContent(
                todayScreenTime = kotlin.time.Duration.parse("1h 30m"),
                dailyLimit = kotlin.time.Duration.parse("3h"),
                hasUsageAccess = true,
                hasAddedApps = true,
                onOpenUsageAccess = {},
            )
        }
    }
}
