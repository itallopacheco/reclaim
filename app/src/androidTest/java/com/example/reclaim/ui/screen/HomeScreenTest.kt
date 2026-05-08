package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.reclaim.ui.theme.ReclaimTheme
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
}
