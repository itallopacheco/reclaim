package com.example.reclaim.domain.apps

import kotlin.time.Duration

class RankAppsForHomeUseCase(
    private val addedApps: AddedAppsRepository,
    private val usageStats: UsageStats,
    private val catalog: AppCatalog,
) {
    fun invoke(): List<HomeAppRow> {
        val catalogMap = catalog.installedApps().associateBy { it.packageName }
        val usageMap = usageStats.usageToday()
        return addedApps.addedApps()
            .map { addedApp ->
                val app = catalogMap.getValue(addedApp.packageName)
                val today = usageMap.getValue(addedApp.packageName)
                HomeAppRow(app, today, addedApp.dailyQuota, status(today, addedApp.dailyQuota))
            }
            .sortedWith(compareByDescending<HomeAppRow> { it.today }.thenBy { it.app.displayName })
    }

    private fun status(today: Duration, quota: Duration): HomeAppStatus = when {
        today > quota -> HomeAppStatus.OVER
        today / quota >= WARN_THRESHOLD -> HomeAppStatus.WARN
        else -> HomeAppStatus.OK
    }

    private companion object {
        const val WARN_THRESHOLD = 0.8
    }
}
