package com.example.reclaim.domain.apps

interface AddedAppsRepository {
    fun addedPackageNames(): Set<String>
}
