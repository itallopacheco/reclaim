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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimTeal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

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
    initialQuota: Duration = QUOTA_DEFAULT,
) {
    var selectedApp by remember { mutableStateOf<App?>(null) }
    var quota by remember { mutableStateOf(initialQuota) }
    var query by remember { mutableStateOf("") }

    fun selectApp(app: App) {
        val previous = selectedApp
        if (previous != null && previous.packageName != app.packageName) {
            quota = QUOTA_DEFAULT
        }
        selectedApp = app
    }
    val suggestions = remember(selectedApp) { suggestApps.invoke() }
    val searchResults = remember(query, selectedApp) {
        if (query.isEmpty()) emptyList() else searchApps.invoke(query)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
    ) {
        Header(
            saveEnabled = selectedApp != null,
            onCancel = onDismiss,
            onSave = {
                val app = selectedApp ?: return@Header
                addedApps.add(AddedApp(app.packageName, quota))
                onSaved()
            },
        )
        Spacer(Modifier.height(20.dp))
        SearchField(query = query, onQueryChange = { query = it })
        Spacer(Modifier.height(20.dp))

        if (selectedApp != null) {
            SectionLabel("SELECTED")
            Spacer(Modifier.height(12.dp))
            SelectedAppCard(
                app = selectedApp!!,
                quota = quota,
                onQuotaIncrease = { quota = (quota + QUOTA_STEP).coerceAtMost(QUOTA_MAX) },
                onQuotaDecrease = { quota = (quota - QUOTA_STEP).coerceAtLeast(QUOTA_MIN) },
            )
            Spacer(Modifier.height(20.dp))
        }

        if (query.isEmpty()) {
            SectionLabel("SUGGESTED · MOST USED")
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { suggestion ->
                    SuggestedAppRow(
                        app = suggestion.app,
                        onClick = { selectApp(suggestion.app) },
                    )
                }
            }
        } else {
            if (searchResults.isEmpty()) {
                Text(
                    text = "No matches",
                    color = ReclaimInk3,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    searchResults.forEach { app ->
                        SuggestedAppRow(
                            app = app,
                            onClick = { selectApp(app) },
                        )
                    }
                }
            }
        }
    }
}

private val QUOTA_STEP = 15.minutes
private val QUOTA_MIN = 15.minutes
private val QUOTA_MAX = 8.hours
private val QUOTA_DEFAULT = 1.hours

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
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ReclaimLine)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Icons.Filled.Search,
            contentDescription = null,
            tint = ReclaimInk3,
            modifier = Modifier.size(17.dp),
        )
        Spacer(Modifier.size(8.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = ReclaimInk,
                    fontSize = 14.5.sp,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Search installed apps" },
            )
            if (query.isEmpty()) {
                Text(
                    text = "Search installed apps",
                    color = ReclaimInk3,
                    fontSize = 14.5.sp,
                )
            }
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
private fun SelectedAppCard(
    app: App,
    quota: Duration,
    onQuotaIncrease: () -> Unit,
    onQuotaDecrease: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIcon(app = app)
            Spacer(Modifier.size(12.dp))
            Text(
                app.displayName,
                color = ReclaimInk,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(16.dp))
        SectionLabel("DAILY QUOTA")
        Spacer(Modifier.height(8.dp))
        QuotaStepper(
            quota = quota,
            onIncrease = onQuotaIncrease,
            onDecrease = onQuotaDecrease,
        )
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
            Text(
                text = "15 min increments",
                color = ReclaimInk3,
                fontSize = 11.sp,
            )
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

private fun formatQuota(quota: Duration): String {
    val totalMinutes = quota.inWholeMinutes
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%dh %02dm".format(h, m)
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
