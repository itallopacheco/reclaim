package com.example.reclaim.domain.apps

import kotlin.time.Duration

class TodayScreenTimeUseCase(
    private val addedApps: AddedAppsRepository,
    private val usageStats: UsageStats,
) {
    fun invoke(): Duration {
        val addedPackages = addedApps.addedApps().map { it.packageName }.toSet()
        return usageStats.usageToday()
            .filter { (pkg, _) -> pkg in addedPackages }
            .values
            .fold(Duration.ZERO) { acc, d -> acc + d }
    }
}
