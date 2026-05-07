package com.example.reclaim.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.reclaim.domain.apps.App
import kotlin.time.Duration

@Composable
fun EditAppSheetContent(
    app: App,
    initialQuota: Duration,
    onSave: (Duration) -> Unit,
) {
    Text(app.displayName)
    Text(formatQuota(initialQuota))
}

private fun formatQuota(quota: Duration): String {
    val totalMinutes = quota.inWholeMinutes
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%dh %02dm".format(h, m)
}
