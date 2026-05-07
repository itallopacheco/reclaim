package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.time.Duration.Companion.minutes

/**
 * Stores one Long preference per added app, keyed by `quota:<packageName>`,
 * holding the daily quota in whole minutes.
 */
class DataStoreAddedAppsRepository(
    private val dataStore: DataStore<Preferences>,
) : AddedAppsRepository {

    override fun addedApps(): List<AddedApp> = runBlocking {
        val prefs = dataStore.data.first()
        prefs.asMap().mapNotNull { (key, value) ->
            val name = key.name
            if (!name.startsWith(QUOTA_KEY_PREFIX) || value !is Long) return@mapNotNull null
            AddedApp(
                packageName = name.removePrefix(QUOTA_KEY_PREFIX),
                dailyQuota = value.minutes,
            )
        }
    }

    override fun add(addedApp: AddedApp) = runBlocking {
        dataStore.edit { prefs ->
            prefs[quotaKey(addedApp.packageName)] = addedApp.dailyQuota.inWholeMinutes
        }
        Unit
    }

    private fun quotaKey(packageName: String) = longPreferencesKey(QUOTA_KEY_PREFIX + packageName)

    private companion object {
        const val QUOTA_KEY_PREFIX = "quota:"
    }
}
