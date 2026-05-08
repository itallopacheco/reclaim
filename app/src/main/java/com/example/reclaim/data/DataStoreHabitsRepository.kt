package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.domain.habits.HabitsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import kotlin.time.Duration.Companion.minutes

/**
 * Stores each habit as three keyed preferences under `habit:<id>:{name,icon,reward}`.
 * Today's completions are stored as `completion:<id>` -> minute-of-day. A `completion_day`
 * key tracks the local date the completions belong to; on the first read after the date
 * changes, completions are cleared.
 */
class DataStoreHabitsRepository(
    private val dataStore: DataStore<Preferences>,
    private val today: () -> LocalDate = { LocalDate.now() },
) : HabitsRepository {

    override fun habits(): List<Habit> = runBlocking {
        val prefs = dataStore.data.first()
        val ids = prefs.asMap().keys
            .map { it.name }
            .filter { it.startsWith(HABIT_PREFIX) && it.endsWith(NAME_SUFFIX) }
            .mapNotNull { extractId(it, NAME_SUFFIX) }
        ids.mapNotNull { id ->
            val name = prefs[nameKey(id)] ?: return@mapNotNull null
            val iconName = prefs[iconKey(id)] ?: return@mapNotNull null
            val rewardMinutes = prefs[rewardKey(id)] ?: return@mapNotNull null
            Habit(
                id = id,
                name = name,
                icon = HabitIcon.valueOf(iconName),
                reward = rewardMinutes.minutes,
            )
        }
    }

    override fun add(habit: Habit) = runBlocking {
        dataStore.edit { prefs ->
            prefs[nameKey(habit.id)] = habit.name
            prefs[iconKey(habit.id)] = habit.icon.name
            prefs[rewardKey(habit.id)] = habit.reward.inWholeMinutes
        }
        Unit
    }

    override fun update(habit: Habit) = add(habit)

    override fun delete(id: Long) = runBlocking {
        dataStore.edit { prefs ->
            prefs.remove(nameKey(id))
            prefs.remove(iconKey(id))
            prefs.remove(rewardKey(id))
            prefs.remove(completionKey(id))
        }
        Unit
    }

    override fun completionsToday(): Map<Long, Int> = runBlocking {
        rolloverIfNeeded()
        val prefs = dataStore.data.first()
        prefs.asMap().mapNotNull { (key, value) ->
            val name = key.name
            if (!name.startsWith(COMPLETION_PREFIX) || value !is Int) return@mapNotNull null
            val id = name.removePrefix(COMPLETION_PREFIX).toLongOrNull() ?: return@mapNotNull null
            id to value
        }.toMap()
    }

    override fun markCompleteToday(id: Long, atMinuteOfDay: Int) = runBlocking {
        rolloverIfNeeded()
        dataStore.edit { prefs ->
            prefs[completionKey(id)] = atMinuteOfDay
            prefs[completionDayKey] = today().toString()
        }
        Unit
    }

    override fun unmarkToday(id: Long) = runBlocking {
        rolloverIfNeeded()
        dataStore.edit { prefs ->
            prefs.remove(completionKey(id))
        }
        Unit
    }

    private suspend fun rolloverIfNeeded() {
        val storedDay = dataStore.data.first()[completionDayKey]
        val currentDay = today().toString()
        if (storedDay != null && storedDay != currentDay) {
            dataStore.edit { prefs ->
                val staleKeys = prefs.asMap().keys
                    .filter { it.name.startsWith(COMPLETION_PREFIX) }
                prefs.remove(completionDayKey)
                staleKeys.forEach { prefs.remove(it) }
            }
        }
    }

    private fun nameKey(id: Long) = stringPreferencesKey("$HABIT_PREFIX$id$NAME_SUFFIX")
    private fun iconKey(id: Long) = stringPreferencesKey("$HABIT_PREFIX$id$ICON_SUFFIX")
    private fun rewardKey(id: Long) = longPreferencesKey("$HABIT_PREFIX$id$REWARD_SUFFIX")
    private fun completionKey(id: Long) = intPreferencesKey("$COMPLETION_PREFIX$id")
    private val completionDayKey = stringPreferencesKey(COMPLETION_DAY_KEY)

    private fun extractId(keyName: String, suffix: String): Long? {
        val withoutPrefix = keyName.removePrefix(HABIT_PREFIX)
        val withoutSuffix = withoutPrefix.removeSuffix(suffix)
        return withoutSuffix.toLongOrNull()
    }

    private companion object {
        const val HABIT_PREFIX = "habit:"
        const val NAME_SUFFIX = ":name"
        const val ICON_SUFFIX = ":icon"
        const val REWARD_SUFFIX = ":reward"
        const val COMPLETION_PREFIX = "completion:"
        const val COMPLETION_DAY_KEY = "completion_day"
    }
}
