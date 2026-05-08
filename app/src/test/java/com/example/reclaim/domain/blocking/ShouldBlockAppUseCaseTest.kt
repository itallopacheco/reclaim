package com.example.reclaim.domain.blocking

import com.example.reclaim.domain.apps.AddedApp
import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeUsageStats
import com.example.reclaim.domain.rewards.fakes.FakeRewardsRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class ShouldBlockAppUseCaseTest {

    private val emptyRewards get() = FakeRewardsRepository()

    @Test
    fun `returns false when package is not added`() {
        val repo = FakeAddedAppsRepository(initialPackageNames = emptySet())
        val stats = FakeUsageStats(avgs = emptyMap(), today = emptyMap())
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

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
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertFalse(useCase.invoke("x"))
    }

    @Test
    fun `returns true when usage today equals quota and balance is zero`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("x" to 30.minutes),
        )
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertTrue(useCase.invoke("x"))
    }

    @Test
    fun `returns true when usage today exceeds quota and balance is zero`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("x" to 45.minutes),
        )
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertTrue(useCase.invoke("x"))
    }

    @Test
    fun `returns false when added with positive quota but no usage recorded today`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = emptyMap())
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertFalse(useCase.invoke("x"))
    }

    @Test
    fun `returns true when quota is zero and balance is zero`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = Duration.ZERO))
        }
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("x" to Duration.ZERO),
        )
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertTrue(useCase.invoke("x"))
    }

    @Test
    fun `returns true when quota is zero and no usage entry and balance is zero`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = Duration.ZERO))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = emptyMap())
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertTrue(useCase.invoke("x"))
    }

    @Test
    fun `returns false when usage exceeds quota but reward balance is positive`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = mapOf("x" to 45.minutes))
        val rewards = FakeRewardsRepository(initial = 5.minutes)
        val useCase = ShouldBlockAppUseCase(repo, stats, rewards)

        assertFalse(useCase.invoke("x"))
    }

    @Test
    fun `returns true when usage exceeds quota and reward balance is negative`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = mapOf("x" to 45.minutes))
        val rewards = FakeRewardsRepository(initial = (-5).minutes)
        val useCase = ShouldBlockAppUseCase(repo, stats, rewards)

        assertTrue(useCase.invoke("x"))
    }

    @Test
    fun `returns false when below quota even if balance is zero`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = 30.minutes))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = mapOf("x" to 25.minutes))
        val useCase = ShouldBlockAppUseCase(repo, stats, emptyRewards)

        assertFalse(useCase.invoke("x"))
    }

    @Test
    fun `returns false when quota is zero and balance is positive`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp(packageName = "x", dailyQuota = Duration.ZERO))
        }
        val stats = FakeUsageStats(avgs = emptyMap(), today = emptyMap())
        val rewards = FakeRewardsRepository(initial = 10.minutes)
        val useCase = ShouldBlockAppUseCase(repo, stats, rewards)

        assertFalse(useCase.invoke("x"))
    }
}
