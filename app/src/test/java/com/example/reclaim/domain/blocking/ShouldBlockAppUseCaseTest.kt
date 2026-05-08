package com.example.reclaim.domain.blocking

import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeUsageStats
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration.Companion.minutes

class ShouldBlockAppUseCaseTest {

    @Test
    fun `returns false when package is not added`() {
        val repo = FakeAddedAppsRepository(initialPackageNames = emptySet())
        val stats = FakeUsageStats(avgs = emptyMap(), today = emptyMap())
        val useCase = ShouldBlockAppUseCase(repo, stats)

        assertFalse(useCase.invoke("com.example.unknown"))
    }

    @Test
    fun `returns false when usage today is below quota`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("x" to 29.minutes),
        )
        val useCase = ShouldBlockAppUseCase(repo, stats)

        assertFalse(useCase.invoke("x"))
    }

    @Test
    fun `returns true when usage today equals quota`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("x" to 30.minutes),
        )
        val useCase = ShouldBlockAppUseCase(repo, stats)

        assertTrue(useCase.invoke("x"))
    }
}
