package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DataStoreRewardsRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val today: () -> LocalDate = { LocalDate.of(2026, 5, 8) }

    @After
    fun tearDown() {
        scope.cancel()
    }

    private fun newDataStore(name: String = "rewards.preferences_pb"): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { tmpFolder.newFile(name) },
        )

    @Test
    fun `currentBalance reflects accumulated rewards`() {
        val repository = DataStoreRewardsRepository(newDataStore(), today)

        repository.addReward(30.minutes)
        repository.addReward(15.minutes)

        assertEquals(45.minutes, repository.currentBalance())
    }

    @Test
    fun `currentBalance returns zero on a new local day`() {
        val file = tmpFolder.newFile("day-change.preferences_pb")
        var current = LocalDate.of(2026, 5, 8)

        val firstScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val firstStore = PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file })
        val day1 = DataStoreRewardsRepository(firstStore, today = { current })
        day1.addReward(20.minutes)
        runBlocking { firstScope.coroutineContext[Job]!!.cancelAndJoin() }

        current = LocalDate.of(2026, 5, 9)
        val day2 = DataStoreRewardsRepository(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
            today = { current },
        )

        assertEquals(Duration.ZERO, day2.currentBalance())
    }

    @Test
    fun `subtractReward may persist a negative balance same day`() {
        val file = tmpFolder.newFile("negative.preferences_pb")

        val firstScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val firstStore = PreferenceDataStoreFactory.create(scope = firstScope, produceFile = { file })
        val first = DataStoreRewardsRepository(firstStore, today)
        first.addReward(5.minutes)
        first.subtractReward(10.minutes)
        runBlocking { firstScope.coroutineContext[Job]!!.cancelAndJoin() }

        val survivor = DataStoreRewardsRepository(
            PreferenceDataStoreFactory.create(scope = scope, produceFile = { file }),
            today,
        )

        assertEquals((-5).minutes, survivor.currentBalance())
    }

    @Test
    fun `spend never drives the persisted balance below zero`() {
        val repository = DataStoreRewardsRepository(newDataStore(), today)
        repository.addReward(30.seconds)

        repository.spend(90.seconds)

        assertEquals(Duration.ZERO, repository.currentBalance())
    }
}
