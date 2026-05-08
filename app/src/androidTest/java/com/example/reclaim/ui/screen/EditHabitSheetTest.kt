package com.example.reclaim.ui.screen

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.reclaim.domain.habits.Habit
import com.example.reclaim.domain.habits.HabitIcon
import com.example.reclaim.ui.theme.ReclaimTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditHabitSheetTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val read = Habit(1L, "Read", HabitIcon.BOOK_OPEN, 30.minutes)

    @Test
    fun preFillsTheCurrentName() {
        composeRule.setContent {
            ReclaimTheme {
                EditHabitSheetContent(
                    habit = read,
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                    onRequestRemove = {},
                )
            }
        }

        composeRule.onNodeWithText("Read").assertIsDisplayed()
    }

    @Test
    fun savingPassesEditedFields() {
        var captured: Triple<String, HabitIcon, Duration>? = null
        composeRule.setContent {
            ReclaimTheme {
                EditHabitSheetContent(
                    habit = read,
                    onDismiss = {},
                    onSave = { name, icon, reward -> captured = Triple(name, icon, reward) },
                    onRequestRemove = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Habit name").performTextClearance()
        composeRule.onNodeWithContentDescription("Habit name").performTextInput("Reader")
        composeRule.onNodeWithContentDescription("Icon SUN").performClick()
        composeRule.onNodeWithText("45").performClick()
        composeRule.onNodeWithText("Save").performClick()

        assertEquals(Triple("Reader", HabitIcon.SUN, 45.minutes), captured)
    }

    @Test
    fun confirmingRemoveFiresOnRequestRemove() {
        var removed = false
        composeRule.setContent {
            ReclaimTheme {
                EditHabitSheetContent(
                    habit = read,
                    onDismiss = {},
                    onSave = { _, _, _ -> },
                    onRequestRemove = { removed = true },
                )
            }
        }

        composeRule.onNodeWithText("Remove habit").performClick()
        composeRule.onNodeWithContentDescription("Confirm remove habit").performClick()

        assertTrue(removed)
    }
}
