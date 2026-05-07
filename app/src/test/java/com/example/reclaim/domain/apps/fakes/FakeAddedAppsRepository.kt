package com.example.reclaim.domain.apps.fakes

import com.example.reclaim.domain.apps.AddedAppsRepository

class FakeAddedAppsRepository(private val packages: Set<String> = emptySet()) : AddedAppsRepository {
    override fun addedPackageNames() = packages
}
