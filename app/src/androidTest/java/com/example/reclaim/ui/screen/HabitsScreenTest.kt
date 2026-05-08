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
    fun listRendersPendingAndCompletedSectionsAndEarnedCard() {
        val read = Habit(1L, "Read 20 minutes", HabitIcon.BOOK_OPEN, 30.minutes)
        val workout = Habit(2L, "Workout", HabitIcon.DUMBBELL, 45.minutes)
        val meditate = Habit(3L, "Morning meditation", HabitIcon.SUN, 15.minutes)
        val journal = Habit(4L, "Journal", HabitIcon.PEN_LINE, 10.minutes)
        composeRule.setContent {
            ReclaimTheme {
                HabitsScreenContent(
                    habits = listOf(read, workout, meditate, journal),
                    completions = mapOf(3L to 7 * 60 + 24, 4L to 8 * 60 + 2),
                    summary = HabitsTodaySummary(25.minutes, 2, 4, 75.minutes),
                    onToggleComplete = {},
                    onEditHabit = {},
                )
            }
        }

        composeRule.onNodeWithText("EARNED TODAY").assertIsDisplayed()
        composeRule.onNodeWithText("+25 min").assertIsDisplayed()
        composeRule.onNodeWithText("2 of 4 done").assertIsDisplayed()
        composeRule.onNodeWithText("+75 min available").assertIsDisplayed()
        composeRule.onNodeWithText("Pending · 2").assertIsDisplayed()
        composeRule.onNodeWithText("Completed · 2").assertIsDisplayed()
        composeRule.onNodeWithText("Read 20 minutes").assertIsDisplayed()
        composeRule.onNodeWithText("Workout").assertIsDisplayed()
        composeRule.onNodeWithText("Morning meditation").assertIsDisplayed()
        composeRule.onNodeWithText("Journal").assertIsDisplayed()
    }

    @Test
    fun tappingCheckCircleFiresToggleCallbackWithHabitId() {
        val read = Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes)
        var toggled: Long? = null
        composeRule.setContent {
            ReclaimTheme {
                HabitsScreenContent(
                    habits = listOf(read),
                    completions = emptyMap(),
                    summary = HabitsTodaySummary(Duration.ZERO, 0, 1, 30.minutes),
                    onToggleComplete = { toggled = it },
                    onEditHabit = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mark Read complete").performClick()

        assertEquals(1L, toggled)
    }

    @Test
    fun tappingHabitRowFiresEditCallbackWithHabitId() {
        val read = Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes)
        var edited: Long? = null
        composeRule.setContent {
            ReclaimTheme {
                HabitsScreenContent(
                    habits = listOf(read),
                    completions = emptyMap(),
                    summary = HabitsTodaySummary(Duration.ZERO, 0, 1, 30.minutes),
                    onToggleComplete = {},
                    onEditHabit = { edited = it },
                )
            }
        }

        composeRule.onNodeWithText("Read").performClick()

        assertEquals(1L, edited)
    }

    @Test
    fun togglingCompleteImmediatelyMovesRowAcrossSections() {
        val read = Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes)
        val repo = com.example.reclaim.ui.screen.fakes.FakeHabitsRepository(initial = listOf(read))
        val summaryUseCase = com.example.reclaim.domain.habits.HabitsTodaySummaryUseCase(repo)

        composeRule.setContent {
            ReclaimTheme {
                HabitsScreen(
                    habitsRepository = repo,
                    summaryUseCase = summaryUseCase,
                    onEditHabit = {},
                )
            }
        }

        composeRule.onNodeWithText("Pending · 1").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Mark Read complete").performClick()

        composeRule.onNodeWithText("Completed · 1").assertIsDisplayed()
    }

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
