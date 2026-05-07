package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.domain.apps.App
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration.Companion.hours
import org.junit.Rule
import org.junit.Test

class EditAppSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun rendersTheAppDisplayName() {
        composeRule.setContent {
            ReclaimTheme {
                EditAppSheetContent(
                    app = App("com.google.youtube", "YouTube", isLauncherApp = true),
                    initialQuota = 1.hours,
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("YouTube").assertIsDisplayed()
    }

    @Test
    fun rendersInitialQuota() {
        composeRule.setContent {
            ReclaimTheme {
                EditAppSheetContent(
                    app = App("com.google.youtube", "YouTube", isLauncherApp = true),
                    initialQuota = 1.hours,
                    onSave = {},
                )
            }
        }

        composeRule.onNodeWithText("1h 00m").assertIsDisplayed()
    }
}
