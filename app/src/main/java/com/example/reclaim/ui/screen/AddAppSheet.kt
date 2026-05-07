package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
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
    var selectedApp by remember { mutableStateOf<App?>(null) }
    val suggestions = remember(selectedApp) { suggestApps.invoke() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Header(
            saveEnabled = selectedApp != null,
            onCancel = onDismiss,
            onSave = onSaved,
        )
        Spacer(Modifier.height(20.dp))

        if (selectedApp != null) {
            SectionLabel("SELECTED")
            Spacer(Modifier.height(12.dp))
            SelectedAppCard(app = selectedApp!!)
            Spacer(Modifier.height(20.dp))
        }

        SectionLabel("SUGGESTED · MOST USED")
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                SuggestedAppRow(
                    app = suggestion.app,
                    onClick = { selectedApp = suggestion.app },
                )
            }
        }
    }
}

@Composable
private fun Header(
    saveEnabled: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel", color = ReclaimInk2, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        Text("Add app", color = ReclaimInk, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.weight(1f))
        TextButton(onClick = onSave, enabled = saveEnabled) {
            Text(
                "Save",
                color = if (saveEnabled) ReclaimTeal else ReclaimInk3,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = ReclaimInk3,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
    )
}

@Composable
private fun SelectedAppCard(app: App) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app = app)
        Spacer(Modifier.size(12.dp))
        Text(
            app.displayName,
            color = ReclaimInk,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SuggestedAppRow(app: App, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(app = app)
        Spacer(Modifier.size(12.dp))
        Text(
            app.displayName,
            color = ReclaimInk,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.SemiBold,
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
