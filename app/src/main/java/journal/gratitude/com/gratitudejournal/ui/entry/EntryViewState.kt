package journal.gratitude.com.gratitudejournal.ui.entry

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class EntryViewState(
    val date: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
    val content: String = "",
    val isEditingExistingEntry: Boolean = false,
    val isInEditMode: Boolean = false,
    val entryNumber: Int? = null,
    val promptNumber: Int? = null,
    val shouldShowMilestoneDialog: Boolean = false,
    val shouldShowQuote: Boolean = true,
    val undoStack: ArrayDeque<String> = ArrayDeque(),
    val redoStack: ArrayDeque<String> = ArrayDeque()
) {
    val userCanUndo = undoStack.isNotEmpty()
    val userCanRedo = redoStack.isNotEmpty()
}
