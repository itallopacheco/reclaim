package com.example.reclaim.data

import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitsRepository
import com.example.reclaim.domain.rewards.RewardsRepository

class RewardingHabitsRepository(
    private val inner: HabitsRepository,
    private val rewards: RewardsRepository,
) : HabitsRepository by inner {

    override fun markCompleteToday(id: Long, atMinuteOfDay: Int) {
        val habit = findHabit(id) ?: return
        inner.markCompleteToday(id, atMinuteOfDay)
        rewards.addReward(habit.reward)
    }

    override fun unmarkToday(id: Long) {
        val habit = findHabit(id) ?: return
        val wasCompleted = id in inner.completionsToday()
        inner.unmarkToday(id)
        if (wasCompleted) rewards.subtractReward(habit.reward)
    }

    private fun findHabit(id: Long): Habit? = inner.habits().firstOrNull { it.id == id }
}
