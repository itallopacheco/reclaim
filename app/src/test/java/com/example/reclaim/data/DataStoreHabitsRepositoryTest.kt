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
}
