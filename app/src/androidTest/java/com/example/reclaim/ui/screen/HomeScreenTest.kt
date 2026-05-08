package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun ringLabelsShowScreenTimeAndDailyLimitCaption() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 1.hours + 30.minutes,
                    dailyLimit = 3.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithText("1h 30m").assertIsDisplayed()
        composeRule.onNodeWithText("of your 3h daily limit").assertIsDisplayed()
    }

    @Test
    fun screenTimeNumberIsMarkedExceededWhenOverLimit() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 2.hours + 14.minutes,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Screen time today, exceeded").assertIsDisplayed()
    }

    @Test
    fun emptyStateShowsAddAppsHintInsteadOfDailyLimitCaption() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = Duration.ZERO,
                    dailyLimit = Duration.ZERO,
                    hasUsageAccess = true,
                    hasAddedApps = false,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithText("Add apps to set your daily limit").assertIsDisplayed()
        composeRule.onAllNodesWithText("of your 0h daily limit").assertCountEquals(0)
    }

    @Test
    fun emptyStateShowsZeroHoursZeroMinutes() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = Duration.ZERO,
                    dailyLimit = Duration.ZERO,
                    hasUsageAccess = true,
                    hasAddedApps = false,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithText("0h 0m").assertIsDisplayed()
    }
}
