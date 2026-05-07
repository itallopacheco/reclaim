package com.example.reclaim.domain.apps

import com.example.reclaim.domain.apps.fakes.FakeAddedAppsRepository
import com.example.reclaim.domain.apps.fakes.FakeAppCatalog
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchAppsUseCaseTest {

    private val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
    private val whatsapp = App("com.whatsapp", "WhatsApp", isLauncherApp = true)
    private val tiktok = App("com.zhiliaoapp.musically", "TikTok", isLauncherApp = true)

    @Test
    fun `matches by case-insensitive substring on display name`() {
        val catalog = FakeAppCatalog(listOf(instagram, whatsapp, tiktok))
        val addedRepo = FakeAddedAppsRepository()

        val result = SearchAppsUseCase(catalog, addedRepo).invoke("what")

        assertEquals(listOf(whatsapp), result)
    }

    @Test
    fun `matches uppercase queries the same as lowercase`() {
        val catalog = FakeAppCatalog(listOf(instagram, whatsapp, tiktok))
        val addedRepo = FakeAddedAppsRepository()

        val result = SearchAppsUseCase(catalog, addedRepo).invoke("WHAT")

        assertEquals(listOf(whatsapp), result)
    }
}
