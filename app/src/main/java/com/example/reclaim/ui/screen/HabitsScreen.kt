package com.example.reclaim.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitsRepository
import com.example.reclaim.domain.habits.HabitsTodaySummary
import com.example.reclaim.domain.habits.HabitsTodaySummaryUseCase
import com.example.reclaim.ui.theme.ReclaimBg
import com.example.reclaim.ui.theme.ReclaimInk
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration

@Composable
fun HabitsScreen(
    habitsRepository: HabitsRepository,
    summaryUseCase: HabitsTodaySummaryUseCase,
    onEditHabit: (Long) -> Unit,
    refreshTick: Int = 0,
) {
    @Suppress("UNUSED_EXPRESSION") refreshTick
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
            .padding(24.dp),
    ) {
        Text(
            text = "Habits",
            color = ReclaimInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
        )
        if (habits.isEmpty()) {
            Text(text = "Create your first habit to start earning time.")
            return@Column
        }
        EarnedTodayCard(summary)
        val (pending, done) = habits.partition { it.id !in completions.keys }
        if (pending.isNotEmpty()) {
            Text(text = "Pending · ${pending.size}")
            pending.forEach { habit ->
                HabitRow(
                    habit = habit,
                    completedAtMinute = null,
                    onToggleComplete = onToggleComplete,
                    onEditHabit = onEditHabit,
                )
            }
        }
        if (done.isNotEmpty()) {
            Text(text = "Completed · ${done.size}")
            done.forEach { habit ->
                HabitRow(
                    habit = habit,
                    completedAtMinute = completions[habit.id],
                    onToggleComplete = onToggleComplete,
                    onEditHabit = onEditHabit,
                )
            }
        }
    }
}

@Composable
private fun EarnedTodayCard(summary: HabitsTodaySummary) {
    Column {
        Text(text = "EARNED TODAY")
        Text(text = "+${summary.earned.inWholeMinutes} min")
        Text(text = "${summary.completed} of ${summary.total} done")
        Text(text = "+${summary.available.inWholeMinutes} min available")
    }
}

@Composable
private fun HabitRow(
    habit: Habit,
    completedAtMinute: Int?,
    onToggleComplete: (Long) -> Unit,
    onEditHabit: (Long) -> Unit,
) {
    Row {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable { onToggleComplete(habit.id) }
                .semantics {
                    contentDescription = if (completedAtMinute == null) {
                        "Mark ${habit.name} complete"
                    } else {
                        "Mark ${habit.name} pending"
                    }
                },
        )
        Text(
            text = habit.name,
            modifier = Modifier.clickable { onEditHabit(habit.id) },
        )
    }
}

private fun currentMinuteOfDay(): Int {
    val now = java.time.LocalTime.now()
    return now.hour * 60 + now.minute
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
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
