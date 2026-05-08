package com.example.reclaim.data

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowUsageStatsManager
import kotlin.time.Duration.Companion.seconds

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UsageEventsForegroundAppMonitorTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @Test
    fun `returns null when there are no recent events`() {
        val monitor = UsageEventsForegroundAppMonitor(usageStatsManager)

        assertNull(monitor.currentForegroundPackage())
    }

    @Test
    fun `returns the package of the last ACTIVITY_RESUMED event`() {
        val now = System.currentTimeMillis()
        val shadow = shadowOf(usageStatsManager)
        shadow.addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setPackage("com.example.first")
                .setEventType(UsageEvents.Event.ACTIVITY_RESUMED)
                .setTimeStamp(now - 4.seconds.inWholeMilliseconds)
                .build()
        )
        shadow.addEvent(
            ShadowUsageStatsManager.EventBuilder.buildEvent()
                .setPackage("com.example.second")
                .setEventType(UsageEvents.Event.ACTIVITY_RESUMED)
                .setTimeStamp(now - 1.seconds.inWholeMilliseconds)
                .build()
        )
        val monitor = UsageEventsForegroundAppMonitor(usageStatsManager)

        assertEquals("com.example.second", monitor.currentForegroundPackage())
    }
}
