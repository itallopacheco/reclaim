package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.reclaim.ui.theme.ReclaimTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class OnboardingPermissionsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsBothPermissionsAsPendingWhenNeitherGranted() {
        composeRule.setContent {
            ReclaimTheme {
                OnboardingPermissionsScreenContent(
                    usageAccessGranted = false,
                    overlayPermissionGranted = false,
                    onOpenUsageAccess = {},
                    onOpenOverlaySettings = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithText("Usage access").assertIsDisplayed()
        composeRule.onNodeWithText("Display over apps").assertIsDisplayed()
        composeRule.onAllNodesWithText("Pending")[0].assertIsDisplayed()
        composeRule.onAllNodesWithText("Pending")[1].assertIsDisplayed()
    }

    @Test
    fun showsGrantedLabelForUsageAccessWhenGranted() {
        composeRule.setContent {
            ReclaimTheme {
                OnboardingPermissionsScreenContent(
                    usageAccessGranted = true,
                    overlayPermissionGranted = false,
                    onOpenUsageAccess = {},
                    onOpenOverlaySettings = {},
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithText("Granted").assertIsDisplayed()
        composeRule.onNodeWithText("Pending").assertIsDisplayed()
    }

    @Test
    fun grantOverlayButtonInvokesOpenOverlaySettings() {
        var openCount = 0
        composeRule.setContent {
            ReclaimTheme {
                OnboardingPermissionsScreenContent(
                    usageAccessGranted = false,
                    overlayPermissionGranted = false,
                    onOpenUsageAccess = {},
                    onOpenOverlaySettings = { openCount++ },
                    onContinue = {},
                )
            }
        }

        composeRule.onNodeWithText("Grant overlay").performClick()

        assertEquals(1, openCount)
    }

    @Test
    fun continueIsClickableEvenWithPendingPermissions() {
        var continueCount = 0
        composeRule.setContent {
            ReclaimTheme {
                OnboardingPermissionsScreenContent(
                    usageAccessGranted = false,
                    overlayPermissionGranted = false,
                    onOpenUsageAccess = {},
                    onOpenOverlaySettings = {},
                    onContinue = { continueCount++ },
                )
            }
        }

        composeRule.onNodeWithText("Continue").performClick()

        assertEquals(1, continueCount)
    }
}
