package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration

@Composable
fun HomeScreen(onSeeAllApps: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .padding(24.dp)
    ) {
        Text(
            text = "Home",
            color = ReclaimInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "(design pending — screen 3)",
            color = ReclaimInk.copy(alpha = 0.5f),
            fontSize = 12.sp
        )
        Spacer(Modifier.height(24.dp))
        TextButton(onClick = onSeeAllApps) {
            Text("See all apps →", color = ReclaimTeal, fontSize = 14.sp)
        }
    }
}

private fun formatScreenTime(d: Duration): String {
    if (d == Duration.ZERO) return "0h 0m"
    val h = d.inWholeHours
    val m = (d.inWholeMinutes % 60).toInt()
    return if (m == 0) "${h}h" else "${h}h ${m}m"
}

@Composable
internal fun HomeScreenContent(
    todayScreenTime: Duration,
    dailyLimit: Duration,
    hasUsageAccess: Boolean,
    hasAddedApps: Boolean,
    onOpenUsageAccess: () -> Unit,
) {
    val exceeded = hasUsageAccess && todayScreenTime > dailyLimit
    Column {
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
    ReclaimTheme { HomeScreen(onSeeAllApps = {}) }
}
