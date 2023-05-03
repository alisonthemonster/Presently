package journal.gratitude.com.gratitudejournal.robot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

class EntryRobot(
    val composeTestRule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>
) {
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val yesterday = today.minus(1, DateTimeUnit.DAY)

    fun assertCorrectDateIsShown(expectedDate: LocalDate) {
        val expectedDateString = if (expectedDate == today) {
            "Today"
        } else if (expectedDate == yesterday) {
            "Yesterday"
        } else {
            expectedDate.toStringWithDayOfWeek()
        }

        composeTestRule.onNodeWithText(expectedDateString).assertIsDisplayed()
    }

    fun assertCorrectTenseIsUsed(expectedDate: LocalDate) {
        val expectedGratitudeString = if (expectedDate == today) {
            "I am grateful for"
        } else {
            "I was grateful for"
        }
        composeTestRule.onNodeWithText(expectedGratitudeString).assertIsDisplayed()
    }

    fun assertCorrectQuestionTense(expectedDate: LocalDate?) {
        val expectedGratitudeString = if (expectedDate == today) {
            "What are you grateful for?"
        } else {
            "What were you grateful for?"
        }
        composeTestRule.onNodeWithTag("question").assertTextEquals(expectedGratitudeString)
    }

    fun assertUserIsInViewMode() {
        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
    }

    fun enterEditMode() {
        composeTestRule.onNodeWithContentDescription("Edit").performClick()
    }

    fun exitEditMode() {
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }

    // assumes user has not typed yet, so undo/redo are not enabled
    fun assertUserIsInEditMode() {
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Undo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Undo").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Redo").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Redo").assertIsNotEnabled()
        composeTestRule.onNodeWithContentDescription("Get a prompt").assertIsDisplayed()
    }

    fun type(text: String) {
        composeTestRule.onNodeWithTag("editViewTextField").performTextInput(text)
    }

    fun assertEntryEditTextEquals(text: String) {
        composeTestRule.onNodeWithTag("editViewTextField").assertTextEquals(text)
    }

    fun assertEntryReadTextEquals(text: String) {
        composeTestRule.onNodeWithTag("readMode").assertIsDisplayed()
        composeTestRule.onNodeWithText(text).assertIsDisplayed()
    }

    fun assertUndoIsEnabled() {
        composeTestRule.onNodeWithContentDescription("Undo").assertIsEnabled()
    }

    fun clickUndo() {
        assertUndoIsEnabled()
        composeTestRule.onNodeWithContentDescription("Undo").performClick()
    }

    fun assertRedoIsEnabled() {
        composeTestRule.onNodeWithContentDescription("Redo").assertIsEnabled()
    }

    fun clickRedo() {
        assertRedoIsEnabled()
        composeTestRule.onNodeWithContentDescription("Redo").performClick()
    }

    fun exitEntryScreen() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
    }

    fun clickBackButton() {
        composeTestRule.onNodeWithContentDescription("Back").performClick()
    }

    fun assertNotInEditMode() {
        composeTestRule.onNodeWithContentDescription("Undo").assertDoesNotExist()
        composeTestRule.onNodeWithContentDescription("Redo").assertDoesNotExist()
    }

    fun clickPromptButton() {
        composeTestRule.onNodeWithContentDescription("Get a prompt").performClick()
    }

    fun assertHintWasSet() {
        val presentTense = "I am grateful for"
        val pastTense = "I was grateful for"

        composeTestRule.onNodeWithTag(presentTense).assertDoesNotExist()
        composeTestRule.onNodeWithText(pastTense).assertDoesNotExist()
    }
}
