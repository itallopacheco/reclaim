package com.example.reclaim.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import com.example.reclaim.domain.blocking.ForegroundAppMonitor
import kotlin.time.Duration.Companion.seconds

class UsageEventsForegroundAppMonitor(
    private val usageStatsManager: UsageStatsManager,
) : ForegroundAppMonitor {

    override fun currentForegroundPackage(): String? {
        val now = System.currentTimeMillis()
        val from = now - LOOKBACK.inWholeMilliseconds
        val events = usageStatsManager.queryEvents(from, now)
        var lastResumedPackage: String? = null
        var lastResumedAt: Long = Long.MIN_VALUE
        val event = UsageEvents.Event()
        while (events.getNextEvent(event)) {
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED && event.timeStamp >= lastResumedAt) {
                lastResumedAt = event.timeStamp
                lastResumedPackage = event.packageName
            }
        }
        return lastResumedPackage
    }

    private companion object {
        val LOOKBACK = 10.seconds
    }
}
