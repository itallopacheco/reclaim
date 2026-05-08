package com.example.reclaim.domain.habits

import com.example.reclaim.domain.habits.fakes.FakeHabitsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class HabitsTodaySummaryUseCaseTest {

    @Test
    fun `earned is the sum of rewards of habits completed today`() {
        val read = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)
        val workout = Habit(id = 2L, name = "Workout", icon = HabitIcon.DUMBBELL, reward = 45.minutes)
        val repo = FakeHabitsRepository(
            initial = listOf(read, workout),
            initialCompletions = mapOf(1L to 7 * 60 + 24),
        )

        val summary = HabitsTodaySummaryUseCase(repo).invoke()

        assertEquals(30.minutes, summary.earned)
    }
}
