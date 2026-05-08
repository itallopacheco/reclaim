package com.example.reclaim.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Process
import com.example.reclaim.domain.apps.UsageStats
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private data class ActiveSpan(val refcount: Int, val openedAt: Long)

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

    override fun usageToday(): Map<String, Duration> {
        if (!hasUsageAccess()) return emptyMap()
        val now = System.currentTimeMillis()
        val startOfToday = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val events = usageStatsManager.queryEvents(startOfToday, now)
        val openByPackage = mutableMapOf<String, ActiveSpan>()
        val totals = mutableMapOf<String, Long>()
        val event = UsageEvents.Event()
        while (events.getNextEvent(event)) {
            val pkg = event.packageName
            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    val current = openByPackage[pkg]
                    openByPackage[pkg] = if (current == null) {
                        ActiveSpan(refcount = 1, openedAt = event.timeStamp)
                    } else {
                        current.copy(refcount = current.refcount + 1)
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val current = openByPackage[pkg] ?: continue
                    if (current.refcount <= 1) {
                        totals.merge(pkg, event.timeStamp - current.openedAt, Long::plus)
                        openByPackage.remove(pkg)
                    } else {
                        openByPackage[pkg] = current.copy(refcount = current.refcount - 1)
                    }
                }
            }
        }
        for ((pkg, span) in openByPackage) {
            totals.merge(pkg, now - span.openedAt, Long::plus)
        }
        return totals.mapValues { (_, ms) -> ms.milliseconds }
    }

    fun hasUsageAccess(): Boolean {
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
