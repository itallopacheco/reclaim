package com.example.reclaim.domain.blocking

import com.example.reclaim.domain.apps.AddedAppsRepository
import com.example.reclaim.domain.apps.UsageStats
import kotlin.time.Duration

class ShouldBlockAppUseCase(
    private val addedApps: AddedAppsRepository,
    private val usageStats: UsageStats,
) {
    fun invoke(packageName: String): Boolean {
        val added = addedApps.addedApps().firstOrNull { it.packageName == packageName } ?: return false
        val today = usageStats.usageToday()[packageName] ?: Duration.ZERO
        return today >= added.dailyQuota
    }
}
