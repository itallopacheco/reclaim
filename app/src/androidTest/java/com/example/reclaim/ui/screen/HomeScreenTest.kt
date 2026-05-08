package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.reclaim.domain.apps.App
import com.example.reclaim.domain.apps.HomeAppRow
import com.example.reclaim.domain.apps.HomeAppStatus
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.domain.habits.HabitsTodaySummary
import com.example.reclaim.ui.theme.ReclaimTheme
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertTrue
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
    fun noUsageAccessShowsEmDashAndGrantButton() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = Duration.ZERO,
                    dailyLimit = 2.hours,
                    hasUsageAccess = false,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithText("—").assertIsDisplayed()
        composeRule.onNodeWithText("Grant usage access").assertIsDisplayed()
    }

    @Test
    fun tappingGrantUsageAccessFiresCallback() {
        var fired = false
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = Duration.ZERO,
                    dailyLimit = 2.hours,
                    hasUsageAccess = false,
                    hasAddedApps = true,
                    onOpenUsageAccess = { fired = true },
                )
            }
        }

        composeRule.onNodeWithText("Grant usage access").performClick()

        assertTrue(fired)
    }

    @Test
    fun headerShowsUppercaseDateAndGoodAfternoonGreeting() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    today = LocalDate.of(2026, 5, 5),
                    todayScreenTime = 1.hours + 30.minutes,
                    dailyLimit = 3.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                )
            }
        }

        composeRule.onNodeWithText("TUESDAY, MAY 5").assertIsDisplayed()
        composeRule.onNodeWithText("Good afternoon").assertIsDisplayed()
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

    @Test
    fun topAppsRowMarksAmberStatusWhenNearQuota() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 1.hours + 45.minutes,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    topApps = listOf(
                        HomeAppRow(instagram, 1.hours + 45.minutes, 2.hours, HomeAppStatus.WARN),
                    ),
                    onSeeAllApps = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Instagram, near quota").assertIsDisplayed()
    }

    @Test
    fun topAppsRowMarksRedStatusWhenExceeded() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 2.hours + 14.minutes,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    topApps = listOf(
                        HomeAppRow(instagram, 2.hours + 14.minutes, 2.hours, HomeAppStatus.OVER),
                    ),
                    onSeeAllApps = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Instagram, exceeded").assertIsDisplayed()
    }

    @Test
    fun tappingSeeAllFiresCallback() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        var fired = false
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 1.hours,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    topApps = listOf(
                        HomeAppRow(instagram, 1.hours, 2.hours, HomeAppStatus.OK),
                    ),
                    onSeeAllApps = { fired = true },
                )
            }
        }

        composeRule.onNodeWithText("See all").performClick()

        assertTrue(fired)
    }

    @Test
    fun earnedPillHiddenWhenNoMinutesEarnedToday() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 1.hours,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    habitsSummary = HabitsTodaySummary(
                        earned = Duration.ZERO,
                        completed = 0,
                        total = 2,
                        available = 30.minutes,
                        nextPending = Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes),
                    ),
                )
            }
        }

        composeRule.onAllNodesWithText("earned today", substring = true).assertCountEquals(0)
    }

    @Test
    fun earnedPillShowsFormattedMinutesWhenEarnedAboveZero() {
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 1.hours,
                    dailyLimit = 2.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    habitsSummary = HabitsTodaySummary(
                        earned = 25.minutes,
                        completed = 1,
                        total = 2,
                        available = 30.minutes,
                        nextPending = Habit(2L, "Workout", HabitIcon.DUMBBELL, 30.minutes),
                    ),
                )
            }
        }

        composeRule.onNodeWithText("+ 25 min earned today").assertIsDisplayed()
    }

    @Test
    fun topAppsSectionRendersRowsRanked() {
        val instagram = App("com.instagram.android", "Instagram", isLauncherApp = true)
        val tiktok = App("com.zhiliaoapp.musically", "TikTok", isLauncherApp = true)
        composeRule.setContent {
            ReclaimTheme {
                HomeScreenContent(
                    todayScreenTime = 2.hours + 30.minutes,
                    dailyLimit = 3.hours,
                    hasUsageAccess = true,
                    hasAddedApps = true,
                    onOpenUsageAccess = {},
                    topApps = listOf(
                        HomeAppRow(instagram, 2.hours, 1.hours, HomeAppStatus.OVER),
                        HomeAppRow(tiktok, 30.minutes, 2.hours, HomeAppStatus.OK),
                    ),
                    onSeeAllApps = {},
                )
            }
        }

        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("TikTok").assertIsDisplayed()
        composeRule.onNodeWithText("2h / 1h").assertIsDisplayed()
    }
}
