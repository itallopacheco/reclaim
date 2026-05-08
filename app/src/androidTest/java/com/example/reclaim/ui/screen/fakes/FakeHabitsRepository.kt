package com.example.reclaim.ui.screen.fakes

import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitsRepository

class FakeHabitsRepository(
    initial: List<Habit> = emptyList(),
    initialCompletions: Map<Long, Int> = emptyMap(),
) : HabitsRepository {

    private val habits = initial.toMutableList()
    private val completions = initialCompletions.toMutableMap()

    override fun habits(): List<Habit> = habits.toList()

    override fun add(habit: Habit) {
        habits.removeAll { it.id == habit.id }
        habits.add(habit)
    }

    override fun update(habit: Habit) {
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index >= 0) habits[index] = habit
    }

    override fun delete(id: Long) {
        habits.removeAll { it.id == id }
        completions.remove(id)
    }

    override fun completionsToday(): Map<Long, Int> = completions.toMap()

    override fun markCompleteToday(id: Long, atMinuteOfDay: Int) {
        completions[id] = atMinuteOfDay
    }

    override fun unmarkToday(id: Long) {
        completions.remove(id)
    }
}
