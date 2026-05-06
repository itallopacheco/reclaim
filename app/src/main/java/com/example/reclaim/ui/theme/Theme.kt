package com.example.reclaim.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ReclaimColorScheme = lightColorScheme(
    primary = ReclaimTeal,
    onPrimary = ReclaimBg,
    secondary = ReclaimInk,
    onSecondary = ReclaimBg,
    background = ReclaimBg,
    onBackground = ReclaimInk,
    surface = ReclaimBg,
    onSurface = ReclaimInk,
    surfaceVariant = ReclaimCanvas,
    onSurfaceVariant = ReclaimInk2,
    outline = ReclaimLine,
    outlineVariant = ReclaimLine2,
    error = ReclaimRed,
    onError = ReclaimBg
)

@Composable
fun ReclaimTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReclaimColorScheme,
        typography = Typography,
        content = content
    )
}
