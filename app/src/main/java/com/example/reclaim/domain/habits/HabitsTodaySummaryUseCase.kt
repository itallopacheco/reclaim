package com.example.reclaim.domain.habits

import kotlin.time.Duration

class HabitsTodaySummaryUseCase(private val habits: HabitsRepository) {
    fun invoke(): HabitsTodaySummary {
        val all = habits.habits()
        val completedIds = habits.completionsToday().keys
        val (done, pending) = all.partition { it.id in completedIds }
        return HabitsTodaySummary(
            earned = done.fold(Duration.ZERO) { acc, h -> acc + h.reward },
            completed = done.size,
            total = all.size,
            available = pending.fold(Duration.ZERO) { acc, h -> acc + h.reward },
            nextPending = pending.firstOrNull(),
        )
    }
}
