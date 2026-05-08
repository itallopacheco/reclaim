package com.example.reclaim.domain.apps

import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeUsageStats
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class TodayScreenTimeUseCaseTest {

    @Test
    fun `sums usage only for added apps`() {
        val repo = FakeAddedAppsRepository(initialPackageNames = setOf("com.instagram.android", "com.zhiliaoapp.musically"))
        val stats = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf(
                "com.instagram.android" to 1.hours,
                "com.zhiliaoapp.musically" to 30.minutes,
                "com.google.android.youtube" to 2.hours,
            ),
        )
        val useCase = TodayScreenTimeUseCase(repo, stats)

        val result = useCase.invoke()

        assertEquals(1.hours + 30.minutes, result)
    }

    @Test
    fun `returns ZERO when no apps have been added`() {
        val repo = FakeAddedAppsRepository()
        val stats = FakeUsageStats(avgs = emptyMap(), today = mapOf("com.foo" to 1.hours))
        val useCase = TodayScreenTimeUseCase(repo, stats)

        val result = useCase.invoke()

        assertEquals(Duration.ZERO, result)
    }
}
