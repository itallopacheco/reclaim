package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration.Companion.minutes
import org.junit.Rule
import org.junit.Test

class BlockedAppScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersAppNameUsageQuotaAndExitButton() {
        composeRule.setContent {
            ReclaimTheme {
                BlockedAppScreenContent(
                    appName = "Instagram",
                    usageToday = 35.minutes,
                    dailyQuota = 30.minutes,
                    onExit = {},
                )
            }
        }

        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("35 min de 30 min").assertIsDisplayed()
        composeRule.onNodeWithText("Sair").assertIsDisplayed()
    }
}
