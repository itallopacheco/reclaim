package com.example.reclaim.domain.habits

interface HabitsRepository {
    fun habits(): List<Habit>
    fun add(habit: Habit)
    fun update(habit: Habit)
    fun delete(id: Long)
    fun completionsToday(): Map<Long, Int>
    fun markCompleteToday(id: Long, atMinuteOfDay: Int)
    fun unmarkToday(id: Long)
}
