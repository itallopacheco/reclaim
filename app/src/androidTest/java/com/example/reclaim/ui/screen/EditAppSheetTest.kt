package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
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

    @Test
    fun tappingSaveCallsOnSaveWithCurrentQuota() {
        var savedQuota: Duration? = null

        composeRule.setContent {
            ReclaimTheme {
                EditAppSheetContent(
                    app = App("com.google.youtube", "YouTube", isLauncherApp = true),
                    initialQuota = 1.hours,
                    onSave = { savedQuota = it },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Increase quota").performClick()
        composeRule.onNodeWithText("Save").performClick()

        composeRule.runOnIdle {
            assert(savedQuota == 1.hours + 15.minutes) {
                "expected onSave with 1h 15m, got $savedQuota"
            }
        }
    }
}
