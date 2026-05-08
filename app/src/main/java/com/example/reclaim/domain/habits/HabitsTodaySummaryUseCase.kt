package com.example.reclaim.domain.habits

import kotlin.time.Duration

class HabitsTodaySummaryUseCase(private val habits: HabitsRepository) {
    fun invoke(): HabitsTodaySummary {
        val completedIds = habits.completionsToday().keys
        val earned = habits.habits()
            .filter { it.id in completedIds }
            .fold(Duration.ZERO) { acc, h -> acc + h.reward }
        return HabitsTodaySummary(
            earned = earned,
            completed = 0,
            total = 0,
            available = Duration.ZERO,
        )
    }
}
