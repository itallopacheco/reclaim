package com.example.reclaim.domain.habits

import com.example.reclaim.domain.habits.fakes.FakeHabitsRepository
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
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

    @Test
    fun `summary reports completed count, total count, and available minutes`() {
        val a = Habit(1L, "A", HabitIcon.BOOK_OPEN, 30.minutes)
        val b = Habit(2L, "B", HabitIcon.DUMBBELL, 45.minutes)
        val c = Habit(3L, "C", HabitIcon.SUN, 15.minutes)
        val d = Habit(4L, "D", HabitIcon.PEN_LINE, 10.minutes)
        val repo = FakeHabitsRepository(
            initial = listOf(a, b, c, d),
            initialCompletions = mapOf(1L to 0, 3L to 0),
        )

        val summary = HabitsTodaySummaryUseCase(repo).invoke()

        assertEquals(
            HabitsTodaySummary(
                earned = 45.minutes,
                completed = 2,
                total = 4,
                available = 55.minutes,
                nextPending = b,
            ),
            summary,
        )
    }

    @Test
    fun `nextPending is the first pending habit in creation order`() {
        val a = Habit(1L, "A", HabitIcon.BOOK_OPEN, 30.minutes)
        val b = Habit(2L, "B", HabitIcon.DUMBBELL, 45.minutes)
        val c = Habit(3L, "C", HabitIcon.SUN, 15.minutes)
        val repo = FakeHabitsRepository(
            initial = listOf(a, b, c),
            initialCompletions = mapOf(1L to 0),
        )

        val summary = HabitsTodaySummaryUseCase(repo).invoke()

        assertEquals(b, summary.nextPending)
    }

    @Test
    fun `empty repository yields zero summary`() {
        val repo = FakeHabitsRepository()

        val summary = HabitsTodaySummaryUseCase(repo).invoke()

        assertEquals(
            HabitsTodaySummary(
                earned = Duration.ZERO,
                completed = 0,
                total = 0,
                available = Duration.ZERO,
                nextPending = null,
            ),
            summary,
        )
    }
}
