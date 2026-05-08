package com.example.reclaim.data

import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.domain.habits.fakes.FakeHabitsRepository
import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class RewardingHabitsRepositoryTest {

    @Test
    fun `markCompleteToday adds the habit reward to the balance`() {
        val read = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)
        val inner = FakeHabitsRepository(initial = listOf(read))
        val rewards = FakeRewardsRepository()
        val repo = RewardingHabitsRepository(inner, rewards)

        repo.markCompleteToday(1L, atMinuteOfDay = 444)

        assertEquals(30.minutes, rewards.currentBalance())
    }

    @Test
    fun `marking the same habit twice in one day adds the reward twice`() {
        val read = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)
        val inner = FakeHabitsRepository(initial = listOf(read))
        val rewards = FakeRewardsRepository()
        val repo = RewardingHabitsRepository(inner, rewards)

        repo.markCompleteToday(1L, atMinuteOfDay = 100)
        repo.markCompleteToday(1L, atMinuteOfDay = 200)

        assertEquals(60.minutes, rewards.currentBalance())
    }

    @Test
    fun `unmarkToday subtracts the habit reward when a completion exists`() {
        val read = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)
        val inner = FakeHabitsRepository(
            initial = listOf(read),
            initialCompletions = mapOf(1L to 444),
        )
        val rewards = FakeRewardsRepository(initial = 30.minutes)
        val repo = RewardingHabitsRepository(inner, rewards)

        repo.unmarkToday(1L)

        assertEquals(Duration.ZERO, rewards.currentBalance())
    }

    @Test
    fun `unmarkToday is a no-op on the balance when no completion exists`() {
        val read = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)
        val inner = FakeHabitsRepository(initial = listOf(read))
        val rewards = FakeRewardsRepository(initial = 5.minutes)
        val repo = RewardingHabitsRepository(inner, rewards)

        repo.unmarkToday(1L)

        assertEquals(5.minutes, rewards.currentBalance())
    }

    @Test
    fun `mark and unmark for an unknown habit do not touch the balance`() {
        val inner = FakeHabitsRepository()
        val rewards = FakeRewardsRepository(initial = 7.minutes)
        val repo = RewardingHabitsRepository(inner, rewards)

        repo.markCompleteToday(99L, atMinuteOfDay = 0)
        repo.unmarkToday(99L)

        assertEquals(7.minutes, rewards.currentBalance())
    }
}
