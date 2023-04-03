package journal.gratitude.com.gratitudejournal.ui.entry

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.EntryArgs
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class EntryyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: EntryRepository,
    private val analytics: AnalyticsLogger,
    private val settings: PresentlySettings,
) : ViewModel() {
    private val _state = MutableStateFlow(EntryViewState())
    val state: StateFlow<EntryViewState> = _state
    val content = state.map { it.content }.distinctUntilChanged().drop(1)

    private val navArgs = EntryArgs(savedStateHandle)

    init {
        val date = navArgs.entryDate.toLocalDate()
        fetchContent(date)

        viewModelScope.launch {
            content.debounce(DEBOUNCE_TIME_MS).collect {
                Log.d("blerg", "debounced, $it")
                writeEntry()
            }
        }
    }

    fun fetchContent(date: LocalDate) {
        viewModelScope.launch {
            val content = repository.getEntry(date)
            _state.value = _state.value.copy(
                date = date,
                content = content?.entryContent ?: ""
            )
        }
    }

    fun logScreenView() {
        analytics.recordView("Entry")
    }

    fun onFabClicked() {
        _state.value = _state.value.copy(isInEditMode = true)
    }

    fun onExitEditMode() {
        _state.value = _state.value.copy(isInEditMode = false)
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun onTextChanged(newText: String) {
        Log.d("blerg", "user typed, $newText")
        onTextChanged(TextChangeType.TYPING, newText)
    }

    fun onUndoClicked() {
        onTextChanged(TextChangeType.UNDO)
    }

    fun onRedoClicked() {
        onTextChanged(TextChangeType.REDO)
    }

    private fun onTextChanged(textChangeType: TextChangeType, newText: String = "") {
        Log.d("blerg", "onTextChanged: $textChangeType")
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
        Log.d("blerg", "onTextChanged: $textChangeType, new text is $textToSave")

        _state.value = _state.value.copy(
            content = textToSave,
            undoStack = undoStack,
            redoStack = redoStack
        )

        //writeEntry()
    }



    private fun writeEntry() {
        val entry =
            journal.gratitude.com.gratitudejournal.model.Entry(
                _state.value.date,
                _state.value.content
            )
        viewModelScope.launch {
            //todo bring back a way to track how many entries a user has
            val numberOfWrittenEntries = repository.addEntry(entry)
//            if (_state.value.isNewEntry) {
//                analytics.recordEntryAdded(numberOfWrittenEntries)
//            } else {
//                analytics.recordEvent(EDITED_EXISTING_ENTRY)
//            }
            Log.d("blerg", "wrote entry: $entry")
            //todo show some indication that data was saved?
            //_state.value = _state.value.copy(saveState = SaveState(numberOfWrittenEntries))
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

    fun onSaveHandled() {
        _state.value = _state.value.copy(saveState = SaveState())
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