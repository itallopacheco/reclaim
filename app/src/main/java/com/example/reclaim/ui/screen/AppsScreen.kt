package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.AppCatalog
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration

private data class AppEntry(
    val packageName: String,
    val displayName: String,
    val quota: Duration,
)

@Composable
fun AppsScreen(
    addedApps: AddedAppsRepository,
    catalog: AppCatalog,
    onAppClick: (packageName: String) -> Unit,
) {
    val nameByPackage = catalog.installedApps().associate { it.packageName to it.displayName }
    val entries = addedApps.addedApps().map {
        AppEntry(
            packageName = it.packageName,
            displayName = nameByPackage[it.packageName] ?: it.packageName,
            quota = it.dailyQuota,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .padding(horizontal = 24.dp, vertical = 24.dp)
    ) {
        Text(
            text = "Apps",
            color = ReclaimInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        if (entries.isEmpty()) {
            Text(
                text = "Tap + to start tracking an app.",
                color = ReclaimInk3,
                fontSize = 13.sp,
            )
        } else {
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                entries.forEach { entry ->
                    AppRow(entry = entry, onClick = { onAppClick(entry.packageName) })
                }
            }
        }
    }
}

@Composable
private fun AppRow(entry: AppEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ReclaimBg)
            .border(1.dp, ReclaimLine, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ReclaimInk),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.displayName.take(2),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.displayName, color = ReclaimInk, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                text = formatQuota(entry.quota) + " / day",
                color = ReclaimInk3,
                fontSize = 12.sp,
            )
        }
    }
}

private fun formatQuota(quota: Duration): String {
    val totalMinutes = quota.inWholeMinutes
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AppsScreenPreview() {
    ReclaimTheme {
        AppsScreen(
            addedApps = object : AddedAppsRepository {
                override fun addedApps() = emptyList<com.example.reclaim.domain.apps.AddedApp>()
                override fun add(addedApp: com.example.reclaim.domain.apps.AddedApp) {}
            },
            catalog = object : AppCatalog {
                override fun installedApps() = emptyList<com.example.reclaim.domain.apps.App>()
            },
            onAppClick = {},
        )
    }
}
