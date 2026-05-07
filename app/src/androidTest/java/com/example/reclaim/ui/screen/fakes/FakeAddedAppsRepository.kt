package com.example.reclaim.ui.screen.fakes

import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository

class FakeAddedAppsRepository(initial: List<AddedApp> = emptyList()) : AddedAppsRepository {
    private val apps = initial.toMutableList()

    override fun addedApps(): List<AddedApp> = apps.toList()

    override fun add(addedApp: AddedApp) {
        apps.removeAll { it.packageName == addedApp.packageName }
        apps.add(addedApp)
    }
}
