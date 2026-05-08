package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.domain.habits.HabitsRepository
import com.example.reclaim.domain.habits.HabitsTodaySummary
import com.example.reclaim.domain.habits.HabitsTodaySummaryUseCase
import com.example.reclaim.ui.theme.ReclaimAmber
import com.example.reclaim.ui.theme.ReclaimAmberBg
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimInk2
import com.example.reclaim.ui.theme.ReclaimInk3
import com.example.reclaim.ui.theme.ReclaimLine
import com.example.reclaim.ui.theme.ReclaimTeal
import com.example.reclaim.ui.theme.ReclaimTeal2
import com.example.reclaim.ui.theme.ReclaimTeal3
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

@Composable
fun HabitsScreen(
    habitsRepository: HabitsRepository,
    summaryUseCase: HabitsTodaySummaryUseCase,
    onEditHabit: (Long) -> Unit,
    refreshTick: Int = 0,
) {
    var localTick by remember { mutableIntStateOf(0) }
    @Suppress("UNUSED_EXPRESSION") refreshTick
    @Suppress("UNUSED_EXPRESSION") localTick
    val habits = habitsRepository.habits()
    val completions = habitsRepository.completionsToday()
    val summary = summaryUseCase.invoke()

    HabitsScreenContent(
        habits = habits,
        completions = completions,
        summary = summary,
        onToggleComplete = { id ->
            if (id in completions.keys) habitsRepository.unmarkToday(id)
            else habitsRepository.markCompleteToday(id, currentMinuteOfDay())
            localTick++
        },
        onEditHabit = onEditHabit,
    )
}

@Composable
internal fun HabitsScreenContent(
    habits: List<Habit>,
    completions: Map<Long, Int>,
    summary: HabitsTodaySummary,
    onToggleComplete: (Long) -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ReclaimBg)
            .verticalScroll(rememberScrollState()),
    ) {
        Header()
        if (habits.isEmpty()) {
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Create your first habit to start earning time.",
                color = ReclaimInk3,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 24.dp),
            )
            return@Column
        }
        Spacer(Modifier.height(16.dp))
        EarnedTodayCard(summary)
        val (pending, done) = habits.partition { it.id !in completions.keys }
        if (pending.isNotEmpty()) {
            SectionHeader(label = "Pending · ${pending.size}", topPadding = 24.dp)
            pending.forEach { habit ->
                HabitRow(
                    habit = habit,
                    completedAtMinute = null,
                    onToggleComplete = onToggleComplete,
                    onEditHabit = onEditHabit,
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        if (done.isNotEmpty()) {
            SectionHeader(label = "Completed · ${done.size}", topPadding = 16.dp)
            done.forEach { habit ->
                HabitRow(
                    habit = habit,
                    completedAtMinute = completions[habit.id],
                    onToggleComplete = onToggleComplete,
                    onEditHabit = onEditHabit,
                )
                Spacer(Modifier.height(10.dp))
            }
        }
        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun Header() {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
        Text(
            text = "TODAY",
            color = ReclaimInk3,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.sp,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "Habits",
            color = ReclaimInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun SectionHeader(label: String, topPadding: androidx.compose.ui.unit.Dp) {
    Text(
        text = label,
        color = ReclaimInk3,
        fontSize = 12.5.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = topPadding, bottom = 12.dp),
    )
}

@Composable
private fun EarnedTodayCard(summary: HabitsTodaySummary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ReclaimTeal2)
            .border(1.dp, ReclaimTeal3, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "EARNED TODAY",
                color = ReclaimTeal,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "+${formatRewardMinutes(summary.earned)} min",
                color = ReclaimInk,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${summary.completed} of ${summary.total} done",
                color = ReclaimInk2,
                fontSize = 12.5.sp,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "+${formatRewardMinutes(summary.available)} min available",
                color = ReclaimInk3,
                fontSize = 12.5.sp,
            )
        }
    }
}

@Composable
private fun HabitRow(
    habit: Habit,
    completedAtMinute: Int?,
    onToggleComplete: (Long) -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    val isDone = completedAtMinute != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(ReclaimBg)
            .border(1.dp, ReclaimLine, RoundedCornerShape(14.dp))
            .clickable { onEditHabit(habit.id) }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(
            isDone = isDone,
            label = if (isDone) "Mark ${habit.name} pending" else "Mark ${habit.name} complete",
            onClick = { onToggleComplete(habit.id) },
        )
        Spacer(Modifier.width(12.dp))
        IconTile(icon = habit.icon, muted = isDone)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.name,
                color = ReclaimInk,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = if (isDone) "Done at ${formatClock(completedAtMinute!!)}" else "Daily",
                color = ReclaimInk3,
                fontSize = 11.5.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        RewardBadge(reward = habit.reward)
    }
}

@Composable
private fun CheckCircle(isDone: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(if (isDone) ReclaimTeal else ReclaimBg)
            .border(1.5.dp, if (isDone) ReclaimTeal else ReclaimLine, CircleShape)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        if (isDone) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = ReclaimBg,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun IconTile(icon: HabitIcon, muted: Boolean) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (muted) ReclaimLine else ReclaimTeal2),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = icon.name.first().toString(),
            color = if (muted) ReclaimInk3 else ReclaimTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun RewardBadge(reward: Duration) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ReclaimAmberBg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = "+${reward.inWholeMinutes} min",
            color = ReclaimAmber,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private fun formatClock(minuteOfDay: Int): String {
    val hour24 = minuteOfDay / 60
    val minute = minuteOfDay % 60
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    val ampm = if (hour24 < 12) "AM" else "PM"
    return "%d:%02d %s".format(hour12, minute, ampm)
}

private fun formatRewardMinutes(d: Duration): Int = d.inWholeMinutes.toInt()

private fun currentMinuteOfDay(): Int {
    val now = java.time.LocalTime.now()
    return now.hour * 60 + now.minute
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Empty")
@Composable
private fun HabitsScreenEmptyPreview() {
    ReclaimTheme {
        HabitsScreenContent(
            habits = emptyList(),
            completions = emptyMap(),
            summary = HabitsTodaySummary(Duration.ZERO, 0, 0, Duration.ZERO),
            onToggleComplete = {},
            onEditHabit = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844, name = "Populated")
@Composable
private fun HabitsScreenPopulatedPreview() {
    ReclaimTheme {
        HabitsScreenContent(
            habits = listOf(
                Habit(1L, "Read 20 minutes", HabitIcon.BOOK_OPEN, 30.minutes),
                Habit(2L, "Workout", HabitIcon.DUMBBELL, 45.minutes),
                Habit(3L, "Morning meditation", HabitIcon.SUN, 15.minutes),
                Habit(4L, "Journal", HabitIcon.PEN_LINE, 10.minutes),
            ),
            completions = mapOf(3L to 7 * 60 + 24, 4L to 8 * 60 + 2),
            summary = HabitsTodaySummary(25.minutes, 2, 4, 75.minutes),
            onToggleComplete = {},
            onEditHabit = {},
        )
    }
}
