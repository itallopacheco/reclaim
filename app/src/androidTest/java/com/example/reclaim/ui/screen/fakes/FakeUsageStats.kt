package com.example.reclaim.ui.screen.fakes

import com.example.reclaim.domain.apps.UsageStats
import kotlin.time.Duration

class FakeUsageStats(private val avgs: Map<String, Duration>) : UsageStats {
    override fun avgDailyUsageLast7Days() = avgs
}
