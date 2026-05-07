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
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTheme

@Composable
fun OnboardingPermissionsScreen(
    onOpenUsageAccess: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .padding(24.dp)
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
            text = "Reclaim needs two permissions to track your screen time.",
            color = ReclaimInk2,
            fontSize = 15.sp,
        )
        Spacer(Modifier.height(20.dp))
        PermissionRow(
            icon = { Icon(Icons.Filled.QueryStats, contentDescription = null, tint = ReclaimTeal) },
            title = "Usage access",
            description = "Lets Reclaim see how long you spend in each app, so it can suggest the ones to limit.",
        )
        Spacer(Modifier.height(12.dp))
        PermissionRow(
            icon = { Icon(Icons.Filled.Apps, contentDescription = null, tint = ReclaimTeal) },
            title = "All installed apps",
            description = "Lets Reclaim list every app on your device in the search field.",
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onOpenUsageAccess,
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
            Text("Grant access", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Skip for now", color = ReclaimInk3, fontSize = 14.sp)
        }
    }
}

@Composable
private fun PermissionRow(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.size(28.dp), verticalArrangement = Arrangement.Center) {
            icon()
        }
        Column {
            Text(title, color = ReclaimInk, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(description, color = ReclaimInk2, fontSize = 13.sp)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun OnboardingPermissionsScreenPreview() {
    ReclaimTheme { OnboardingPermissionsScreen(onOpenUsageAccess = {}, onSkip = {}) }
}
