package com.example.reclaim.data

import android.app.AppOpsManager
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

        assertEquals(emptyMap<String, kotlin.time.Duration>(), stats.avgDailyUsageLast7Days())
    }
}
