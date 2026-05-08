package com.example.reclaim.domain.blocking

import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.UsageStats

class ShouldBlockAppUseCase(
    private val addedApps: AddedAppsRepository,
    private val usageStats: UsageStats,
) {
    fun invoke(packageName: String): Boolean = false
}
