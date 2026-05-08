package com.example.reclaim.domain.habits

import kotlin.time.Duration

data class HabitsTodaySummary(
    val earned: Duration,
    val completed: Int,
    val total: Int,
    val available: Duration,
    val nextPending: Habit?,
)
