package journal.gratitude.com.gratitudejournal.ui.entry

import org.threeten.bp.LocalDate

data class EntryViewState(
    val date: LocalDate = LocalDate.now(),
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
