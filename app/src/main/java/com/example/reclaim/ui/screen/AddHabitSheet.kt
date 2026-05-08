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
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimTeal
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

internal val REWARD_CHIPS: List<Duration> =
    listOf(5.minutes, 10.minutes, 15.minutes, 30.minutes, 45.minutes)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, icon: HabitIcon, reward: Duration) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ReclaimBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        AddHabitSheetContent(onDismiss = onDismiss, onSave = onSave)
    }
}

@Composable
internal fun AddHabitSheetContent(
    onDismiss: () -> Unit,
    onSave: (name: String, icon: HabitIcon, reward: Duration) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf(HabitIcon.BOOK_OPEN) }
    var reward by remember { mutableStateOf(15.minutes) }

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
            Text("New habit", color = ReclaimInk, fontWeight = FontWeight.SemiBold)
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
        IconPicker(selected = icon, onSelected = { icon = it })

        Spacer(Modifier.height(20.dp))
        Text(text = "TIME REWARD", color = ReclaimInk3, fontSize = 12.5.sp)
        Spacer(Modifier.height(8.dp))
        RewardChips(selected = reward, onSelected = { reward = it })
        Spacer(Modifier.height(8.dp))
        Text(text = "minutes earned per completion", color = ReclaimInk3, fontSize = 11.5.sp)
    }
}

@Composable
private fun IconPicker(selected: HabitIcon, onSelected: (HabitIcon) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitIcon.values().take(5).forEach { entry ->
            IconTile(entry, selected == entry) { onSelected(entry) }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        HabitIcon.values().drop(5).forEach { entry ->
            IconTile(entry, selected == entry) { onSelected(entry) }
        }
    }
}

@Composable
private fun IconTile(icon: HabitIcon, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) ReclaimTeal else ReclaimLine,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Icon ${icon.name}" },
        contentAlignment = Alignment.Center,
    ) {
        Text(text = icon.name.first().toString(), fontSize = 14.sp, color = ReclaimInk)
    }
}

@Composable
private fun RewardChips(selected: Duration, onSelected: (Duration) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        REWARD_CHIPS.forEach { d ->
            val label = d.inWholeMinutes.toString()
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .border(
                        width = if (selected == d) 2.dp else 1.dp,
                        color = if (selected == d) ReclaimTeal else ReclaimLine,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .clickable { onSelected(d) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = label, color = ReclaimInk, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
