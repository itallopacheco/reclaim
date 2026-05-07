package com.example.reclaim.domain.apps

import kotlin.time.Duration

interface UsageStats {
    fun avgDailyUsageLast7Days(): Map<String, Duration>
}
