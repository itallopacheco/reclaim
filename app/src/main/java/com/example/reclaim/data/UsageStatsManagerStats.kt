package com.example.reclaim.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.os.Process
import com.example.reclaim.domain.apps.UsageStats
import kotlin.time.Duration

class UsageStatsManagerStats(
    private val usageStatsManager: UsageStatsManager,
    private val appOpsManager: AppOpsManager,
    private val packageName: String,
) : UsageStats {

    override fun avgDailyUsageLast7Days(): Map<String, Duration> {
        if (!hasUsageAccess()) return emptyMap()
        return emptyMap()
    }

    private fun hasUsageAccess(): Boolean {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            packageName,
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }
}
