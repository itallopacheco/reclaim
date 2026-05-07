package com.example.reclaim.domain.apps.fakes

import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.AddedAppsRepository
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class FakeAddedAppsRepository(initialPackageNames: Set<String> = emptySet()) : AddedAppsRepository {
    private val apps = initialPackageNames
        .map { AddedApp(it, DEFAULT_QUOTA) }
        .toMutableList()

    override fun addedApps(): List<AddedApp> = apps.toList()

    override fun add(addedApp: AddedApp) {
        apps.removeAll { it.packageName == addedApp.packageName }
        apps.add(addedApp)
    }

    private companion object {
        val DEFAULT_QUOTA: Duration = 1.hours
    }
}
