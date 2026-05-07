package com.example.reclaim.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.os.Process
import com.example.reclaim.domain.apps.UsageStats
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class UsageStatsManagerStats(
    private val usageStatsManager: UsageStatsManager,
    private val appOpsManager: AppOpsManager,
    private val packageName: String,
) : UsageStats {

    override fun avgDailyUsageLast7Days(): Map<String, Duration> {
        if (!hasUsageAccess()) return emptyMap()
        val now = System.currentTimeMillis()
        val weekAgo = now - WEEK_MILLIS
        val raw = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, weekAgo, now)
            ?: return emptyMap()
        return raw
            .groupBy { it.packageName }
            .mapValues { (_, entries) ->
                (entries.sumOf { it.totalTimeInForeground } / DAYS_IN_WEEK).milliseconds
            }
    }

    private fun hasUsageAccess(): Boolean {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private companion object {
        const val DAYS_IN_WEEK = 7L
        const val WEEK_MILLIS = DAYS_IN_WEEK * 24 * 60 * 60 * 1000
    }
}
