package com.example.reclaim.domain.apps

import kotlin.time.Duration

class SuggestAppsUseCase(
    private val catalog: AppCatalog,
    private val usageStats: UsageStats,
    private val addedApps: AddedAppsRepository,
) {
    fun invoke(): List<SuggestedApp> {
        val usage = usageStats.avgDailyUsageLast7Days()
        return catalog.installedApps()
            .map { SuggestedApp(it, usage.getValue(it.packageName)) }
            .filter { it.avgDaily > Duration.ZERO }
            .sortedByDescending { it.avgDaily }
    }
}
