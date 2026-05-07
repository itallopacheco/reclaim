package com.example.reclaim.domain.apps

import kotlin.time.Duration

class SuggestAppsUseCase(
    private val catalog: AppCatalog,
    private val usageStats: UsageStats,
    private val addedApps: AddedAppsRepository,
) {
    fun invoke(): List<SuggestedApp> {
        val usage = usageStats.avgDailyUsageLast7Days()
        val added = addedApps.addedApps().mapTo(HashSet()) { it.packageName }
        return catalog.installedApps()
            .filter { it.isLauncherApp }
            .filter { it.packageName !in added }
            .mapNotNull { app -> usage[app.packageName]?.let { SuggestedApp(app, it) } }
            .filter { it.avgDaily > Duration.ZERO }
            .sortedByDescending { it.avgDaily }
            .take(MAX_SUGGESTIONS)
    }

    private companion object {
        const val MAX_SUGGESTIONS = 10
    }
}
