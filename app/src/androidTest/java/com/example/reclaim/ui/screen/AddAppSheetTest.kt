package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.screen.fakes.FakeAddedAppsRepository
import com.example.reclaim.ui.screen.fakes.FakeAppCatalog
import com.example.reclaim.ui.screen.fakes.FakeUsageStats
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration.Companion.hours
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

    @Test
    fun quotaStepperPlusButtonStopsAt8h() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(listOf(instagram))
        val usage = FakeUsageStats(mapOf(instagram.packageName to 60.minutes))

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {},
                    initialQuota = 7.hours + 45.minutes,
                )
            }
        }

        composeRule.onNodeWithText("Instagram").performClick()
        // 7h45m + 15m = 8h, then plus must be disabled
        composeRule.onNodeWithContentDescription("Increase quota").performClick()
        composeRule.onNodeWithContentDescription("Increase quota").assertIsNotEnabled()
    }

    @Test
    fun quotaStepperMinusButtonStopsAt15min() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(listOf(instagram))
        val usage = FakeUsageStats(mapOf(instagram.packageName to 60.minutes))

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {},
                    initialQuota = 30.minutes,
                )
            }
        }

        composeRule.onNodeWithText("Instagram").performClick()
        // 30m - 15m = 15m, then minus must be disabled
        composeRule.onNodeWithContentDescription("Decrease quota").performClick()
        composeRule.onNodeWithContentDescription("Decrease quota").assertIsNotEnabled()
    }

    @Test
    fun searchFiltersAppsBySubstringIgnoringCase() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val whatsapp = App("com.whatsapp", "WhatsApp", isLauncherApp = true)
        val tiktok = App("com.tiktok", "TikTok", isLauncherApp = true)
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(listOf(instagram, whatsapp, tiktok))
        val usage = FakeUsageStats(
            mapOf(
                instagram.packageName to 60.minutes,
                whatsapp.packageName to 30.minutes,
                tiktok.packageName to 90.minutes,
            )
        )

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Search installed apps")
            .performTextInput("WHAT")

        composeRule.onNodeWithText("WhatsApp").assertIsDisplayed()
        composeRule.onAllNodesWithText("Instagram").assertCountEquals(0)
        composeRule.onAllNodesWithText("TikTok").assertCountEquals(0)
    }

    @Test
    fun switchingSelectedAppResetsQuotaToDefault() {
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
                    onSaved = {},
                    initialQuota = 2.hours + 30.minutes,
                )
            }
        }

        // Pick Instagram (quota will start at 2h 30m).
        composeRule.onNodeWithText("Instagram").performClick()
        composeRule.onNodeWithText("2h 30m").assertIsDisplayed()

        // Switch to WhatsApp; quota must reset to default 1h 00m.
        composeRule.onNodeWithText("WhatsApp").performClick()
        composeRule.onNodeWithText("1h 00m").assertIsDisplayed()
    }

    @Test
    fun searchWithoutMatchesShowsNoMatchesText() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val repo = FakeAddedAppsRepository()
        val catalog = FakeAppCatalog(listOf(instagram))
        val usage = FakeUsageStats(mapOf(instagram.packageName to 60.minutes))

        composeRule.setContent {
            ReclaimTheme {
                AddAppSheetContent(
                    suggestApps = SuggestAppsUseCase(catalog, usage, repo),
                    searchApps = SearchAppsUseCase(catalog, repo),
                    addedApps = repo,
                    onDismiss = {},
                    onSaved = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Search installed apps")
            .performTextInput("xyz")

        composeRule.onNodeWithText("No matches").assertIsDisplayed()
    }
}
