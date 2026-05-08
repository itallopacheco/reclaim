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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTeal
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditHabitSheet(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: HabitIcon, reward: Duration) -> Unit,
    onRequestRemove: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ReclaimBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        EditHabitSheetContent(
            habit = habit,
            onDismiss = onDismiss,
            onSave = onSave,
            onRequestRemove = onRequestRemove,
        )
    }
}

@Composable
internal fun EditHabitSheetContent(
    habit: Habit,
    onDismiss: () -> Unit,
    onSave: (name: String, icon: HabitIcon, reward: Duration) -> Unit,
    onRequestRemove: () -> Unit,
) {
    var name by remember { mutableStateOf(habit.name) }
    var icon by remember { mutableStateOf(habit.icon) }
    var reward by remember { mutableStateOf(habit.reward) }
    var showRemoveDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel", color = ReclaimInk3) }
            Spacer(Modifier.weight(1f))
            Text("Edit habit", color = ReclaimInk, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            TextButton(
                onClick = { onSave(name.trim(), icon, reward) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save", color = ReclaimTeal, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(16.dp))

        Text(text = "NAME", color = ReclaimInk3, fontSize = 12.5.sp)
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, ReclaimLine, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = TextStyle(color = ReclaimInk, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = "Habit name" },
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(text = "ICON", color = ReclaimInk3, fontSize = 12.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitIcon.values().take(5).forEach { entry ->
                EditIconTile(entry, icon == entry) { icon = entry }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HabitIcon.values().drop(5).forEach { entry ->
                EditIconTile(entry, icon == entry) { icon = entry }
            }
        }

        Spacer(Modifier.height(20.dp))
        Text(text = "TIME REWARD", color = ReclaimInk3, fontSize = 12.5.sp)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            REWARD_CHIPS.forEach { d ->
                val label = d.inWholeMinutes.toString()
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .border(
                            width = if (reward == d) 2.dp else 1.dp,
                            color = if (reward == d) ReclaimTeal else ReclaimLine,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickable { reward = d }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, color = ReclaimInk, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        TextButton(onClick = { showRemoveDialog = true }) {
            Text("Remove habit", color = ReclaimRed, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Remove ${habit.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { showRemoveDialog = false; onRequestRemove() },
                    modifier = Modifier.semantics { contentDescription = "Confirm remove habit" },
                ) {
                    Text("Remove", color = ReclaimRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRemoveDialog = false },
                    modifier = Modifier.semantics { contentDescription = "Cancel remove habit" },
                ) {
                    Text("Cancel", color = ReclaimInk2)
                }
            },
            containerColor = ReclaimBg,
        )
    }
}

@Composable
private fun EditIconTile(icon: HabitIcon, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ReclaimTeal else ReclaimLine,
                shape = RoundedCornerShape(12.dp),
            )
            .background(ReclaimBg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Icon ${icon.name}" },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = icon.name.first().toString(), fontSize = 14.sp, color = ReclaimInk)
    }
}
