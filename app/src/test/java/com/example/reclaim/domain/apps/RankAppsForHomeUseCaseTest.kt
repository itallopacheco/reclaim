package com.example.reclaim.domain.apps

import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeAppCatalog
import com.example.reclaim.domain.apps.fakes.FakeUsageStats
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class RankAppsForHomeUseCaseTest {

    @Test
    fun `orders rows by today's usage descending`() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val tiktok = App("com.zhiliaoapp.musically", "TikTok", isLauncherApp = true)
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp("com.instagram.android", 1.hours))
            add(AddedApp("com.zhiliaoapp.musically", 2.hours))
        }
        val usage = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf(
                "com.instagram.android" to 2.hours,
                "com.zhiliaoapp.musically" to 30.minutes,
            ),
        )
        val catalog = FakeAppCatalog(listOf(instagram, tiktok))

        val result = RankAppsForHomeUseCase(repo, usage, catalog).invoke()

        assertEquals(
            listOf(
                HomeAppRow(instagram, 2.hours, 1.hours, HomeAppStatus.OVER),
                HomeAppRow(tiktok, 30.minutes, 2.hours, HomeAppStatus.OK),
            ),
            result,
        )
    }

    @Test
    fun `falls back to package name as display name when not in catalog`() {
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp("com.unknown.app", 1.hours))
        }
        val usage = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf("com.unknown.app" to 30.minutes),
        )
        val catalog = FakeAppCatalog(emptyList())

        val result = RankAppsForHomeUseCase(repo, usage, catalog).invoke()

        assertEquals("com.unknown.app", result.single().app.displayName)
    }

    @Test
    fun `breaks usage ties by display name ascending`() {
        val tiktok = App("com.zhiliaoapp.musically", "TikTok", isLauncherApp = true)
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val repo = FakeAddedAppsRepository().apply {
            add(AddedApp("com.zhiliaoapp.musically", 2.hours))
            add(AddedApp("com.instagram.android", 2.hours))
        }
        val usage = FakeUsageStats(
            avgs = emptyMap(),
            today = mapOf(
                "com.zhiliaoapp.musically" to 30.minutes,
                "com.instagram.android" to 30.minutes,
            ),
        )
        val catalog = FakeAppCatalog(listOf(tiktok, instagram))

        val result = RankAppsForHomeUseCase(repo, usage, catalog).invoke()

        assertEquals(listOf("Instagram", "TikTok"), result.map { it.app.displayName })
    }
}
