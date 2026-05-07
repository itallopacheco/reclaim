package com.example.reclaim.data

import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository

/**
 * Process-lifetime store for apps the user has added. Lost on app restart.
 * Real persistence (DataStore) lands in fatia C.
 */
object InMemoryAddedAppsRepository : AddedAppsRepository {
    private val apps = mutableListOf<AddedApp>()

    override fun addedApps(): List<AddedApp> = apps.toList()

    override fun add(addedApp: AddedApp) {
        apps.removeAll { it.packageName == addedApp.packageName }
        apps.add(addedApp)
    }
}
