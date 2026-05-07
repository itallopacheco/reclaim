package com.example.reclaim.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAppSheet(
    suggestApps: SuggestAppsUseCase,
    searchApps: SearchAppsUseCase,
    addedApps: AddedAppsRepository,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ReclaimBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        AddAppSheetContent(
            suggestApps = suggestApps,
            searchApps = searchApps,
            addedApps = addedApps,
            onDismiss = onDismiss,
            onSaved = onSaved,
        )
    }
}

@Composable
fun AddAppSheetContent(
    suggestApps: SuggestAppsUseCase,
    searchApps: SearchAppsUseCase,
    addedApps: AddedAppsRepository,
    onDismiss: () -> Unit,
    onSaved: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "Add app",
            color = ReclaimInk,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Cancel", color = ReclaimInk3, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Button(
                onClick = onSaved,
                enabled = false,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ReclaimTeal,
                    contentColor = Color.White
                )
            ) {
                Text("Save", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
