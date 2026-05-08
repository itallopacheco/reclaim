package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class AddHabitSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun saveIsDisabledWhenNameIsEmpty() {
        composeRule.setContent {
            ReclaimTheme {
                AddHabitSheetContent(
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
    }

    @Test
    fun typingNameEnablesSave() {
        composeRule.setContent {
            ReclaimTheme {
                AddHabitSheetContent(
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Habit name").performTextInput("Walk outside")

        composeRule.onNodeWithText("Save").assertIsEnabled()
    }

    @Test
    fun savingPassesNameSelectedIconAndDefaultReward() {
        var captured: Triple<String, HabitIcon, Duration>? = null
        composeRule.setContent {
            ReclaimTheme {
                AddHabitSheetContent(
                    onDismiss = {},
                    onSave = { name, icon, reward -> captured = Triple(name, icon, reward) },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Habit name").performTextInput("Walk outside")
        composeRule.onNodeWithText("Save").performClick()

        assertEquals(Triple("Walk outside", HabitIcon.BOOK_OPEN, 15.minutes), captured)
    }

    @Test
    fun pickingIconAndRewardChipUpdatesTheSavedValues() {
        var captured: Triple<String, HabitIcon, Duration>? = null
        composeRule.setContent {
            ReclaimTheme {
                AddHabitSheetContent(
                    onDismiss = {},
                    onSave = { name, icon, reward -> captured = Triple(name, icon, reward) },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Habit name").performTextInput("Workout")
        composeRule.onNodeWithContentDescription("Icon DUMBBELL").performClick()
        composeRule.onNodeWithText("45").performClick()
        composeRule.onNodeWithText("Save").performClick()

        assertEquals(Triple("Workout", HabitIcon.DUMBBELL, 45.minutes), captured)
    }
}
