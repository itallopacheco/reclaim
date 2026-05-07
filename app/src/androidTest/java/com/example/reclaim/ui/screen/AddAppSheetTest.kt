package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.screen.fakes.FakeAddedAppsRepository
import com.example.reclaim.ui.screen.fakes.FakeAppCatalog
import com.example.reclaim.ui.screen.fakes.FakeUsageStats
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration.Companion.minutes
import org.junit.Rule
import org.junit.Test

class AddAppSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun saveButtonIsDisabledWhenNoAppIsSelected() {
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(emptyList())
        val usage = FakeUsageStats(emptyMap())

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {}
                )
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun tappingSuggestedAppMovesItToSelected() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val whatsapp = App("com.whatsapp", "WhatsApp", isLauncherApp = true)
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(listOf(instagram, whatsapp))
        val usage = FakeUsageStats(
            mapOf(
                instagram.packageName to 60.minutes,
                whatsapp.packageName to 30.minutes,
            )
        )

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {}
                )
            }
        }

        composeRule.onNodeWithText("Instagram").performClick()
        composeRule.onNodeWithText("SELECTED").assertIsDisplayed()
    }
}
