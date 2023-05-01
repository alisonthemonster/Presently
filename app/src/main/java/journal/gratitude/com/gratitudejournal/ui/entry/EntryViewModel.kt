package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.navigation.EntryArgs
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EntryRepository,
    private val analytics: AnalyticsLogger,
    private val settings: PresentlySettings,
) : ViewModel() {
    private val _state = MutableStateFlow(EntryViewState())
    val state: StateFlow<EntryViewState> = _state

    private val debouncedText = state.map { it.content }
        .distinctUntilChanged()
        .debounce(DEBOUNCE_TIME_MS)

    private val navArgs = EntryArgs(savedStateHandle)

    init {
        fetchContent(navArgs.entryDate.toLocalDate())

        autosave()

        applyUserSettings()
    }

    private fun applyUserSettings() {
        val shouldShowQuote = settings.shouldShowQuote()
        _state.value = _state.value.copy(shouldShowQuote = shouldShowQuote)
    }

    private fun autosave() {
        viewModelScope.launch {
            debouncedText.collect {
                writeEntry()
            }
        }
    }

    private fun fetchContent(date: LocalDate) {
        viewModelScope.launch {
            val content = repository.getEntry(date)
            _state.value = _state.value.copy(
                date = date,
                content = content?.entryContent ?: "",
                isEditingExistingEntry = content != null,
                isInEditMode = content == null, //go straight to edit mode
            )
        }
    }

    fun logScreenView() {
        analytics.recordView("Entry")
    }

    fun onFabClicked() {
        analytics.recordEvent("EditButtonClicked")
        _state.value = _state.value.copy(isInEditMode = true)
    }

    fun onExitEditMode() {
        val entryNumber = _state.value.entryNumber
        if (entryNumber != null) {
            //the user saved an entry
            if (_state.value.isEditingExistingEntry) {
                analytics.recordEvent(EDITED_EXISTING_ENTRY)
            } else {
                analytics.recordEntryAdded(entryNumber)
            }
        }

        val shouldShowMilestoneDialog =
            !_state.value.isEditingExistingEntry && isMilestone(_state.value.entryNumber ?: -1)
        _state.value = _state.value.copy(
            isInEditMode = false,
            shouldShowMilestoneDialog = shouldShowMilestoneDialog,
        )
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun onTextChanged(newText: String) {
        onTextChanged(TextChangeType.TYPING, newText)
    }

    fun onUndoClicked() {
        onTextChanged(TextChangeType.UNDO)
    }

    fun onRedoClicked() {
        onTextChanged(TextChangeType.REDO)
    }

    private fun onTextChanged(textChangeType: TextChangeType, newText: String = "") {
        val currentText = _state.value.content
        val undoStack = _state.value.undoStack
        val redoStack = _state.value.redoStack

        val textToSave = when (textChangeType) {
            TextChangeType.TYPING -> {
                undoStack.pushWithLimit(currentText, HISTORY_LIMIT)
                redoStack.clear()
                newText
            }
            TextChangeType.UNDO -> {
                redoStack.pushWithLimit(currentText, HISTORY_LIMIT)
                undoStack.removeLast()
            }
            TextChangeType.REDO -> {
                undoStack.pushWithLimit(currentText, HISTORY_LIMIT)
                redoStack.removeLast()
            }
        }

        _state.value = _state.value.copy(
            content = textToSave,
            undoStack = undoStack,
            redoStack = redoStack
        )
    }

    private fun writeEntry() {
        val entry =
            journal.gratitude.com.gratitudejournal.model.Entry(
                _state.value.date,
                _state.value.content
            )
        viewModelScope.launch {
            val numberOfWrittenEntries = repository.addEntry(entry)
            _state.value = _state.value.copy(entryNumber = numberOfWrittenEntries)
            //todo show some indication that data was saved?
        }
    }

    fun changeHint(totalHints: Int) {
        analytics.recordEvent(CLICKED_PROMPT)
        val prevIndex = _state.value.promptNumber
        var rnds = (0 until totalHints).random()
        while (rnds == prevIndex) {
            rnds = (0 until totalHints).random()
        }
        _state.value = _state.value.copy(promptNumber = rnds)
    }

    fun onDismissMilestoneDialog() {
        _state.value = _state.value.copy(shouldShowMilestoneDialog = false)
    }

    companion object {
        private const val DEBOUNCE_TIME_MS = 300L
        private const val HISTORY_LIMIT = 50
    }
}

/**
 *  Pushes an [item] onto the Stack, kicks out the oldest item if there
 *  are too many items in the Stack according to [limit].
 *
 *  @param item the item to push onto the Stack
 *  @param limit the maximum number of items in the Stack
 */
private fun <E> ArrayDeque<E>.pushWithLimit(item: E, limit: Int) {
    this.addLast(item)
    if (this.size > limit) {
        this.removeFirst()
    }
}

enum class TextChangeType {
    TYPING,
    UNDO,
    REDO,
}