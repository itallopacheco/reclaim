package com.example.reclaim.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.TodayScreenTimeUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTeal
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

    HomeScreenContent(
        todayScreenTime = today,
        dailyLimit = limit,
        hasUsageAccess = accessGranted,
        hasAddedApps = added.isNotEmpty(),
        onOpenUsageAccess = onOpenUsageAccess,
    )
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
    val progress = when {
        !hasUsageAccess || !hasAddedApps -> 0f
        else -> (todayScreenTime / dailyLimit).toFloat().coerceIn(0f, 1f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .verticalScroll(rememberScrollState()),
    ) {
        Header(today = today)
        HeroRing(
            progress = progress,
            centerNumber = if (hasUsageAccess) formatScreenTime(todayScreenTime) else "—",
            centerNumberColor = if (exceeded) ReclaimRed else ReclaimInk,
            centerSemantics = if (exceeded) "Screen time today, exceeded" else "Screen time today",
            captionLine1 = "of your",
            captionLine2 = if (hasAddedApps) "${formatScreenTime(dailyLimit)} daily limit" else null,
            emptyHint = if (!hasAddedApps) "Add apps to set your daily limit" else null,
            grantUsageAccess = if (!hasUsageAccess) onOpenUsageAccess else null,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun Header(today: LocalDate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = today.format(HEADER_DATE_FORMATTER).uppercase(Locale.ENGLISH),
                color = ReclaimInk3,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Good afternoon",
                color = ReclaimInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(ReclaimBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                tint = ReclaimInk,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun HeroRing(
    progress: Float,
    centerNumber: String,
    centerNumberColor: androidx.compose.ui.graphics.Color,
    centerSemantics: String,
    captionLine1: String,
    captionLine2: String?,
    emptyHint: String?,
    grantUsageAccess: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(232.dp),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(232.dp)) {
                val strokeWidth = 14.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                val arcSize = Size(diameter, diameter)
                drawArc(
                    color = ReclaimLine,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth),
                )
                if (progress > 0f) {
                    drawArc(
                        color = ReclaimTeal,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                Text(
                    text = "SCREEN TIME TODAY",
                    color = ReclaimInk3,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = centerNumber,
                    color = centerNumberColor,
                    fontSize = 52.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.semantics { contentDescription = centerSemantics },
                )
                Spacer(Modifier.height(8.dp))
                if (captionLine2 != null) {
                    val caption = buildAnnotatedString {
                        withStyle(SpanStyle(color = ReclaimInk2)) {
                            append("$captionLine1 $captionLine2")
                        }
                    }
                    Text(
                        text = caption,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
                if (emptyHint != null) {
                    Text(
                        text = emptyHint,
                        color = ReclaimInk3,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        if (grantUsageAccess != null) {
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = grantUsageAccess,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    text = "Grant usage access",
                    color = ReclaimTeal,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomeScreenPreview() {
    ReclaimTheme {
        HomeScreenContent(
            todayScreenTime = Duration.parse("2h 47m"),
            dailyLimit = Duration.parse("3h"),
            hasUsageAccess = true,
            hasAddedApps = true,
            onOpenUsageAccess = {},
            today = LocalDate.of(2026, 5, 5),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Empty")
@Composable
private fun HomeScreenEmptyPreview() {
    ReclaimTheme {
        HomeScreenContent(
            todayScreenTime = Duration.ZERO,
            dailyLimit = Duration.ZERO,
            hasUsageAccess = true,
            hasAddedApps = false,
            onOpenUsageAccess = {},
            today = LocalDate.of(2026, 5, 5),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "No permission")
@Composable
private fun HomeScreenNoPermissionPreview() {
    ReclaimTheme {
        HomeScreenContent(
            todayScreenTime = Duration.ZERO,
            dailyLimit = Duration.parse("2h"),
            hasUsageAccess = false,
            hasAddedApps = true,
            onOpenUsageAccess = {},
            today = LocalDate.of(2026, 5, 5),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Exceeded")
@Composable
private fun HomeScreenExceededPreview() {
    ReclaimTheme {
        HomeScreenContent(
            todayScreenTime = Duration.parse("2h 14m"),
            dailyLimit = Duration.parse("2h"),
            hasUsageAccess = true,
            hasAddedApps = true,
            onOpenUsageAccess = {},
            today = LocalDate.of(2026, 5, 5),
        )
    }
}
