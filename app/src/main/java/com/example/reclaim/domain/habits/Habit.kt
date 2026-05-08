package com.example.reclaim.domain.habits

import kotlin.time.Duration

data class Habit(
    val id: Long,
    val name: String,
    val icon: HabitIcon,
    val reward: Duration,
)
