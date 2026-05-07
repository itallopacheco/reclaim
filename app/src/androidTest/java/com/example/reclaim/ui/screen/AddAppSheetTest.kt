package com.example.reclaim.ui.screen

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.domain.apps.SearchAppsUseCase
import com.example.reclaim.domain.apps.SuggestAppsUseCase
import com.example.reclaim.ui.screen.fakes.FakeAddedAppsRepository
import com.example.reclaim.ui.screen.fakes.FakeAppCatalog
import com.example.reclaim.ui.screen.fakes.FakeUsageStats
import com.example.reclaim.ui.theme.ReclaimTheme
import org.junit.Rule
import org.junit.Test

class AddAppSheetTest {

    @get:Rule
    val composeRule = createComposeRule()

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
}
