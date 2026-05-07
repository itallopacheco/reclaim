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
}
