package com.example.reclaim.data

import com.example.reclaim.domain.apps.UsageStats
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Static demo usage so Suggested has something to rank before fatia C
 * wires up UsageStatsManager.
 */
object DemoUsageStats : UsageStats {
    override fun avgDailyUsageLast7Days(): Map<String, Duration> = mapOf(
        "com.instagram.android" to 130.minutes,
        "com.whatsapp" to 45.minutes,
        "com.zhiliaoapp.musically" to 105.minutes,
        "com.google.android.youtube" to 58.minutes,
        "com.twitter.android" to 42.minutes,
        "com.snapchat.android" to 18.minutes,
        "com.linkedin.android" to 12.minutes,
        "com.reddit.frontpage" to 35.minutes,
        "com.spotify.music" to 90.minutes,
        "com.netflix.mediaclient" to 0.minutes,
    )
}
