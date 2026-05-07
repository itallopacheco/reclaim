package com.example.reclaim.domain.apps

interface AddedAppsRepository {
    fun addedApps(): List<AddedApp>
    fun add(addedApp: AddedApp)
    fun delete(packageName: String)
}
