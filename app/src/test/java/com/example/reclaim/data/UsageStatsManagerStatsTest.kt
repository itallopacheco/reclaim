package com.example.reclaim.data

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowUsageStatsManager
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UsageStatsManagerStatsTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private fun setUsageAccessMode(mode: Int) {
        shadowOf(appOps).setMode(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName,
            mode,
        )
    }

    @Test
    fun `avgDailyUsageLast7Days is empty when usage access permission is denied`() {
        setUsageAccessMode(AppOpsManager.MODE_IGNORED)
        val stats = UsageStatsManagerStats(
            usageStatsManager = usageStatsManager,
            appOpsManager = appOps,
            packageName = context.packageName,
        )

        assertEquals(emptyMap<String, Duration>(), stats.avgDailyUsageLast7Days())
    }

    @Test
    fun `avgDailyUsageLast7Days divides total foreground time by 7`() {
        setUsageAccessMode(AppOpsManager.MODE_ALLOWED)
        val sevenHoursMillis = 7L * 60 * 60 * 1000
        shadowOf(usageStatsManager).addUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            buildUsageStats("com.example.foo", sevenHoursMillis),
        )
        val stats = UsageStatsManagerStats(usageStatsManager, appOps, context.packageName)

        assertEquals(mapOf("com.example.foo" to 1.hours), stats.avgDailyUsageLast7Days())
    }

    @Test
    fun `avgDailyUsageLast7Days aggregates entries with the same package`() {
        setUsageAccessMode(AppOpsManager.MODE_ALLOWED)
        val threeHoursMillis = 3L * 60 * 60 * 1000
        val fourHoursMillis = 4L * 60 * 60 * 1000
        shadowOf(usageStatsManager).addUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            buildUsageStats("com.example.foo", threeHoursMillis),
        )
        shadowOf(usageStatsManager).addUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            buildUsageStats("com.example.foo", fourHoursMillis),
        )
        val stats = UsageStatsManagerStats(usageStatsManager, appOps, context.packageName)

        assertEquals(mapOf("com.example.foo" to 1.hours), stats.avgDailyUsageLast7Days())
    }

    private fun buildUsageStats(packageName: String, totalForegroundMs: Long): UsageStats {
        val now = System.currentTimeMillis()
        return ShadowUsageStatsManager.UsageStatsBuilder.newBuilder()
            .setPackageName(packageName)
            .setTotalTimeInForeground(totalForegroundMs)
            .setFirstTimeStamp(now - 24 * 60 * 60 * 1000)
            .setLastTimeStamp(now)
            .build()
    }
}
