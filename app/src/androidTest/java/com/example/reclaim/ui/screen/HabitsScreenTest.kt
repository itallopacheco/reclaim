package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.domain.habits.HabitsTodaySummary
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HabitsScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateShowsCreateHintAndHidesEarnedCard() {
        composeRule.setContent {
            ReclaimTheme {
                HabitsScreenContent(
                    habits = emptyList(),
                    completions = emptyMap(),
                    summary = HabitsTodaySummary(Duration.ZERO, 0, 0, Duration.ZERO),
                    onToggleComplete = {},
                    onEditHabit = {},
                )
            }
        }

        composeRule.onNodeWithText("Create your first habit to start earning time.")
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("EARNED TODAY").assertCountEquals(0)
    }
}
