package com.example.reclaim.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository

class DataStoreAddedAppsRepository(
    @Suppress("unused") private val dataStore: DataStore<Preferences>,
) : AddedAppsRepository {

    override fun addedApps(): List<AddedApp> = emptyList()

    override fun add(addedApp: AddedApp) {
        TODO("not implemented")
    }
}
