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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
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
    Column {
        Text(text = formatScreenTime(todayScreenTime))
        Text(text = "of your ${formatScreenTime(dailyLimit)} daily limit")
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    ReclaimTheme { HomeScreen(onSeeAllApps = {}) }
}
