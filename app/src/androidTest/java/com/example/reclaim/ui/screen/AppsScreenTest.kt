package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration.Companion.minutes
import org.junit.Rule
import org.junit.Test

class AppsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsBlockingNowBadgeForBlockedEntry() {
        composeRule.setContent {
            ReclaimTheme {
                AppsScreenContent(
                    entries = listOf(
                        AppEntry(
                            packageName = "com.instagram.android",
                            displayName = "Instagram",
                            quota = 30.minutes,
                            isBlockingNow = true,
                        ),
                    ),
                    onAppClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("Blocking now").assertIsDisplayed()
    }

    @Test
    fun hidesBlockingNowBadgeForNonBlockedEntry() {
        composeRule.setContent {
            ReclaimTheme {
                AppsScreenContent(
                    entries = listOf(
                        AppEntry(
                            packageName = "com.instagram.android",
                            displayName = "Instagram",
                            quota = 30.minutes,
                            isBlockingNow = false,
                        ),
                    ),
                    onAppClick = {},
                )
            }
        }

        composeRule.onAllNodesWithText("Blocking now").assertCountEquals(0)
    }
}
