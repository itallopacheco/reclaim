package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.apps.App
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTeal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun EditAppSheetContent(
    app: App,
    initialQuota: Duration,
    onSave: (Duration) -> Unit,
    onRequestRemove: () -> Unit = {},
) {
    var quota by remember { mutableStateOf(initialQuota) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Header(
            onCancel = {},
            onSave = { onSave(quota) },
        )
        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppIcon(app)
                Spacer(Modifier.size(12.dp))
                Text(
                    app.displayName,
                    color = ReclaimInk,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "DAILY QUOTA",
                color = ReclaimInk3,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(8.dp))
            QuotaStepper(
                quota = quota,
                onIncrease = { quota = (quota + QUOTA_STEP).coerceAtMost(QUOTA_MAX) },
                onDecrease = { quota = (quota - QUOTA_STEP).coerceAtLeast(QUOTA_MIN) },
            )
        }
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = { showRemoveDialog = true }) {
            Text(
                "Remove app",
                color = ReclaimRed,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove ${app.displayName}?") },
            confirmButton = {
                TextButton(
                    onClick = { showRemoveDialog = false; onRequestRemove() },
                    modifier = Modifier.semantics { contentDescription = "Confirm remove app" },
                ) {
                    Text("Remove", color = ReclaimRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false },
                    modifier = Modifier.semantics { contentDescription = "Cancel remove app" },
                ) {
                    Text("Cancel", color = ReclaimInk2)
                }
            },
        )
    }
}

private val QUOTA_STEP = 15.minutes
private val QUOTA_MIN = 15.minutes
private val QUOTA_MAX = 8.hours

@Composable
private fun Header(onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", color = ReclaimInk2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Text("Edit app", color = ReclaimInk, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSave) {
            Text("Save", color = ReclaimTeal, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuotaStepper(
    quota: Duration,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
) {
    val canIncrease = quota < QUOTA_MAX
    val canDecrease = quota > QUOTA_MIN
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        StepperButton(
            icon = Icons.Filled.Remove,
            contentDescription = "Decrease quota",
            enabled = canDecrease,
            onClick = onDecrease,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatQuota(quota),
                color = ReclaimInk,
                fontSize = 34.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(text = "15 min increments", color = ReclaimInk3, fontSize = 11.sp)
        }
        StepperButton(
            icon = Icons.Filled.Add,
            contentDescription = "Increase quota",
            enabled = canIncrease,
            onClick = onIncrease,
        )
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(1.dp, ReclaimLine, CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                if (!enabled) disabled()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (enabled) ReclaimInk else ReclaimInk3,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun AppIcon(app: App) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(ReclaimInk),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = app.displayName.take(2),
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatQuota(quota: Duration): String {
    val totalMinutes = quota.inWholeMinutes
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%dh %02dm".format(h, m)
}
