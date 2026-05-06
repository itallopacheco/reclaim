package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.ui.theme.ReclaimAmber
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimRed
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTeal2
import com.example.reclaim.ui.theme.ReclaimTheme

private data class EarnBackHabit(
    val id: String,
    val name: String,
    val rewardMinutes: Int,
    val icon: ImageVector
)

@Composable
fun LockScreen(
    appName: String = "Instagram",
    appInitials: String = "Ig",
    appBrandColor: Color = ReclaimRed,
    dailyLimitHours: Int = 2,
    onBack: () -> Unit = {},
    onGoToHabits: () -> Unit = {},
    onTakeBreak: () -> Unit = {}
) {
    val habits = remember {
        listOf(
            EarnBackHabit("read", "Read 20 minutes", 30, Icons.AutoMirrored.Filled.MenuBook),
            EarnBackHabit("workout", "Workout", 45, Icons.Filled.FitnessCenter)
        )
    }
    val totalAvailable = habits.sumOf { it.rewardMinutes }
    var completed by remember { mutableStateOf(emptySet<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
    ) {
        TopBar(onBack = onBack)
        Hero(
            appName = appName,
            appInitials = appInitials,
            appBrandColor = appBrandColor,
            dailyLimitHours = dailyLimitHours
        )
        Spacer(Modifier.height(28.dp))
        EarnBackPanel(
            habits = habits,
            completed = completed,
            totalAvailable = totalAvailable,
            onToggle = { id ->
                completed = if (id in completed) completed - id else completed + id
            }
        )
        Spacer(Modifier.weight(1f))
        FooterCta(
            onGoToHabits = onGoToHabits,
            onTakeBreak = onTakeBreak
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.ChevronLeft,
                contentDescription = "Back",
                tint = ReclaimInk,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "paused",
            color = ReclaimInk3,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.size(36.dp))
    }
}

@Composable
private fun Hero(
    appName: String,
    appInitials: String,
    appBrandColor: Color,
    dailyLimitHours: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // CSS used filter: grayscale(0.7); opacity: 0.55 on the brand color.
        // Approximate by desaturating + dimming the brand color toward the ink tone.
        val dimmedBrand = appBrandColor.copy(alpha = 0.55f).compositeOver(ReclaimInk3)
        Box {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(dimmedBrand),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = appInitials,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ReclaimInk)
                    .border(3.dp, ReclaimBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "$appName is paused",
            color = ReclaimInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "You hit your $dailyLimitHours-hour daily limit. Complete a habit to earn time back.",
            color = ReclaimInk2,
            fontSize = 14.5.sp,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}

@Composable
private fun EarnBackPanel(
    habits: List<EarnBackHabit>,
    completed: Set<String>,
    totalAvailable: Int,
    onToggle: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Earn time back",
                color = ReclaimInk,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            AvailablePill(totalAvailable)
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            habits.forEach { habit ->
                HabitRow(
                    habit = habit,
                    checked = habit.id in completed,
                    onToggle = { onToggle(habit.id) }
                )
            }
        }
    }
}

@Composable
private fun AvailablePill(totalMinutes: Int) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ReclaimTeal2)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = ReclaimTeal,
            modifier = Modifier.size(11.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "+$totalMinutes min available",
            color = ReclaimTeal,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun HabitRow(
    habit: EarnBackHabit,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(ReclaimBg)
            .border(1.dp, ReclaimLine, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggle)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CheckCircle(checked)
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ReclaimTeal2),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                habit.icon,
                contentDescription = null,
                tint = ReclaimTeal,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                color = ReclaimInk,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (checked) "Unlocked" else "Mark done to unlock",
                color = if (checked) ReclaimTeal else ReclaimInk3,
                fontSize = 11.5.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        RewardBadge("+${habit.rewardMinutes} min")
    }
}

@Composable
private fun CheckCircle(checked: Boolean) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(if (checked) ReclaimTeal else Color.Transparent)
            .border(
                width = 1.5.dp,
                color = if (checked) ReclaimTeal else ReclaimLine,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun RewardBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ReclaimAmber.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = text,
            color = ReclaimAmber,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FooterCta(
    onGoToHabits: () -> Unit,
    onTakeBreak: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Or wait until tomorrow — your limit resets at midnight.",
            color = ReclaimInk3,
            fontSize = 12.5.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Button(
            onClick = onGoToHabits,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ReclaimTeal,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                Icons.Filled.GpsFixed,
                contentDescription = null,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Go to my habits",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        OutlinedButton(
            onClick = onTakeBreak,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ReclaimInk2)
        ) {
            Text(
                text = "Take a 5-minute break instead",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun LockScreenPreview() {
    ReclaimTheme {
        LockScreen()
    }
}
