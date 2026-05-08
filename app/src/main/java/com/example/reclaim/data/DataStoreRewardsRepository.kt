package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.reclaim.domain.rewards.RewardsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DataStoreRewardsRepository(
    private val dataStore: DataStore<Preferences>,
    private val today: () -> LocalDate = { LocalDate.now() },
) : RewardsRepository {

    override fun currentBalance(): Duration = runBlocking {
        rolloverIfNeeded()
        val seconds = dataStore.data.first()[balanceKey] ?: 0L
        seconds.seconds
    }

    override fun addReward(amount: Duration) = runBlocking {
        mutate { it + amount.inWholeSeconds }
    }

    override fun subtractReward(amount: Duration) = runBlocking {
        mutate { it - amount.inWholeSeconds }
    }

    override fun spend(amount: Duration) = runBlocking {
        mutate { (it - amount.inWholeSeconds).coerceAtLeast(0L) }
    }

    private suspend fun mutate(transform: (Long) -> Long) {
        rolloverIfNeeded()
        dataStore.edit { prefs ->
            val current = prefs[balanceKey] ?: 0L
            prefs[balanceKey] = transform(current)
            prefs[dayKey] = today().toString()
        }
    }

    private suspend fun rolloverIfNeeded() {
        val storedDay = dataStore.data.first()[dayKey]
        val currentDay = today().toString()
        if (storedDay != null && storedDay != currentDay) {
            dataStore.edit { prefs ->
                prefs.remove(balanceKey)
                prefs.remove(dayKey)
            }
        }
    }

    private val balanceKey = longPreferencesKey(BALANCE_KEY)
    private val dayKey = stringPreferencesKey(DAY_KEY)

    private companion object {
        const val BALANCE_KEY = "rewards_balance_seconds"
        const val DAY_KEY = "rewards_day"
    }
}
