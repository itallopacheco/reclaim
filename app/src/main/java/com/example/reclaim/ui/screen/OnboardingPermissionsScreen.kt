package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.reclaim.ui.theme.ReclaimAmber
import com.example.reclaim.ui.theme.ReclaimAmberBg
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimGreen
import com.example.reclaim.ui.theme.ReclaimGreenBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTheme

@Composable
fun OnboardingPermissionsScreen(
    usageAccessGranted: Boolean,
    overlayPermissionGranted: Boolean,
    onOpenUsageAccess: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onContinue: () -> Unit,
) {
    OnboardingPermissionsScreenContent(
        usageAccessGranted = usageAccessGranted,
        overlayPermissionGranted = overlayPermissionGranted,
        onOpenUsageAccess = onOpenUsageAccess,
        onOpenOverlaySettings = onOpenOverlaySettings,
        onContinue = onContinue,
    )
}

@Composable
internal fun OnboardingPermissionsScreenContent(
    usageAccessGranted: Boolean,
    overlayPermissionGranted: Boolean,
    onOpenUsageAccess: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .padding(24.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            text = "Allow access",
            color = ReclaimInk,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Reclaim needs two permissions to track and block apps.",
            color = ReclaimInk2,
            fontSize = 15.sp,
        )
        Spacer(Modifier.height(20.dp))
        PermissionRow(
            icon = { Icon(Icons.Filled.QueryStats, contentDescription = null, tint = ReclaimTeal) },
            title = "Usage access",
            description = "Lets Reclaim see how long you spend in each app.",
            granted = usageAccessGranted,
            grantButtonLabel = "Grant access",
            onGrant = onOpenUsageAccess,
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            icon = { Icon(Icons.Filled.Layers, contentDescription = null, tint = ReclaimTeal) },
            title = "Display over apps",
            description = "Without this, the block screen can't appear over the app you're trying to open.",
            granted = overlayPermissionGranted,
            grantButtonLabel = "Grant overlay",
            onGrant = onOpenOverlaySettings,
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReclaimTeal,
                contentColor = Color.White,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Text("Continue", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun PermissionRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    granted: Boolean,
    grantButtonLabel: String,
    onGrant: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.size(28.dp), verticalArrangement = Arrangement.Center) {
                icon()
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = ReclaimInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(description, color = ReclaimInk2, fontSize = 13.sp)
            }
            StatusPill(granted = granted)
        }
        if (!granted) {
            OutlinedButton(
                onClick = onGrant,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ReclaimTeal),
            ) {
                Text(grantButtonLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun StatusPill(granted: Boolean) {
    val (label, fg, bg) = if (granted) {
        Triple("Granted", ReclaimGreen, ReclaimGreenBg)
    } else {
        Triple("Pending", ReclaimAmber, ReclaimAmberBg)
    }
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, color = fg, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPermissionsScreenPreview() {
    ReclaimTheme {
        OnboardingPermissionsScreenContent(
            usageAccessGranted = false,
            overlayPermissionGranted = false,
            onOpenUsageAccess = {},
            onOpenOverlaySettings = {},
            onContinue = {},
        )
    }
}
