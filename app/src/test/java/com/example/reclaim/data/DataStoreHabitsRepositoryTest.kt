package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

class DataStoreHabitsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val today: () -> LocalDate = { LocalDate.of(2026, 5, 8) }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun newDataStore(name: String = "habits.preferences_pb"): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmpFolder.newFile(name) },
        )

    @Test
    fun `habits is empty on a fresh store`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)

        assertTrue(repository.habits().isEmpty())
    }

    @Test
    fun `add then habits returns the saved habit with all fields`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)
        val habit = Habit(id = 1L, name = "Read", icon = HabitIcon.BOOK_OPEN, reward = 30.minutes)

        repository.add(habit)

        assertEquals(listOf(habit), repository.habits())
    }

    @Test
    fun `update replaces a habit's fields by id`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)
        repository.add(Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes))

        repository.update(Habit(1L, "Read more", HabitIcon.SUN, 45.minutes))

        assertEquals(listOf(Habit(1L, "Read more", HabitIcon.SUN, 45.minutes)), repository.habits())
    }

    @Test
    fun `delete removes the habit and its completion`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)
        repository.add(Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes))
        repository.markCompleteToday(1L, atMinuteOfDay = 7 * 60 + 24)

        repository.delete(1L)

        assertTrue(repository.habits().isEmpty())
        assertTrue(repository.completionsToday().isEmpty())
    }

    @Test
    fun `markCompleteToday records the minute of day for the habit`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)
        repository.add(Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes))

        repository.markCompleteToday(1L, atMinuteOfDay = 444)

        assertEquals(mapOf(1L to 444), repository.completionsToday())
    }

    @Test
    fun `unmarkToday removes the completion`() {
        val repository = DataStoreHabitsRepository(newDataStore(), today)
        repository.add(Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes))
        repository.markCompleteToday(1L, atMinuteOfDay = 444)

        repository.unmarkToday(1L)

        assertTrue(repository.completionsToday().isEmpty())
    }

    @Test
    fun `completionsToday clears entries when the local date has changed`() {
        val file = tmpFolder.newFile("day-change.preferences_pb")
        val day1 = LocalDate.of(2026, 5, 8)
        val day2 = LocalDate.of(2026, 5, 9)
        var current = day1

        val firstScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val firstStore = PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file })
        val first = DataStoreHabitsRepository(firstStore, today = { current })
        first.add(Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes))
        first.markCompleteToday(1L, atMinuteOfDay = 444)
        runBlocking { firstScope.coroutineContext[Job]!!.cancelAndJoin() }

        current = day2
        val survivor = DataStoreHabitsRepository(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
            today = { current },
        )

        assertTrue(survivor.completionsToday().isEmpty())
        assertEquals(1, survivor.habits().size)
    }
}
