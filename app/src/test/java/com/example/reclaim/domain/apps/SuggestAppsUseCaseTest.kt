package com.example.reclaim.domain.apps

import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeAppCatalog
import com.example.reclaim.domain.apps.fakes.FakeUsageStats
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Test

class SuggestAppsUseCaseTest {

    @Test
    fun `suggests apps ordered by avg daily usage descending`() {
        val catalog = FakeAppCatalog(
            listOf(
                App("com.a", "Alpha", isLauncherApp = true),
                App("com.b", "Beta", isLauncherApp = true),
                App("com.c", "Gamma", isLauncherApp = true),
            )
        )
        val usageStats = FakeUsageStats(
            mapOf(
                "com.a" to 45.minutes,
                "com.b" to 20.minutes,
                "com.c" to 90.minutes,
            )
        )
        val addedRepo = FakeAddedAppsRepository()

        val result = SuggestAppsUseCase(catalog, usageStats, addedRepo).invoke()

        assertEquals(
            listOf(
                SuggestedApp(App("com.c", "Gamma", isLauncherApp = true), 90.minutes),
                SuggestedApp(App("com.a", "Alpha", isLauncherApp = true), 45.minutes),
                SuggestedApp(App("com.b", "Beta", isLauncherApp = true), 20.minutes),
            ),
            result
        )
    }

    @Test
    fun `excludes apps with zero avg daily usage`() {
        val catalog = FakeAppCatalog(
            listOf(
                App("com.a", "Alpha", isLauncherApp = true),
                App("com.b", "Beta", isLauncherApp = true),
            )
        )
        val usageStats = FakeUsageStats(
            mapOf(
                "com.a" to 60.minutes,
                "com.b" to Duration.ZERO,
            )
        )
        val addedRepo = FakeAddedAppsRepository()

        val result = SuggestAppsUseCase(catalog, usageStats, addedRepo).invoke()

        assertEquals(
            listOf(
                SuggestedApp(App("com.a", "Alpha", isLauncherApp = true), 60.minutes),
            ),
            result
        )
    }
}
