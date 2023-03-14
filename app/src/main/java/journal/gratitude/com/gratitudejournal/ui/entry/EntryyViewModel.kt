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
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class EntryyViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: EntryRepository,
    private val analytics: AnalyticsLogger,
    private val settings: PresentlySettings,
) : ViewModel() {
    private val _state = MutableStateFlow(EntryViewState())
    val state: StateFlow<EntryViewState> = _state

    private val navArgs = EntryArgs(savedStateHandle)

    init {
        val date = navArgs.entryDate.toLocalDate()
        fetchContent(date)
    }

    fun fetchContent(date: LocalDate) {
        Log.d("blerg", "gonna fetch content")
        viewModelScope.launch {
            val content = repository.getEntry(date)
            Log.d("blerg", "got some data: ${content?.entryContent}")
            _state.value = _state.value.copy(
                date = date,
                content = content?.entryContent ?: ""
            )
        }
    }

    fun logScreenView() {
        Log.d("blerg", "entry screen view")
        analytics.recordView("Entry")
    }

    fun handleEvent(entryEvent: EntryEvent) {
        when (entryEvent) {
            EntryEvent.OnHintClicked -> changeHint()
            EntryEvent.OnSaveClicked -> saveEntry()
            is EntryEvent.OnTextChanged -> {
                _state.value = _state.value.copy(content = entryEvent.newText)
            }
        }
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    private fun saveEntry() {
        val entry =
            journal.gratitude.com.gratitudejournal.model.Entry(
                _state.value.date,
                _state.value.content
            )
        viewModelScope.launch {
            repository.addEntry(entry)
        }
        if (_state.value.isNewEntry) {
            //todo find a way to log this again
            //analytics.recordEntryAdded(totalEntries)
        } else {
            analytics.recordEvent(EDITED_EXISTING_ENTRY)
        }
        _state.value = _state.value.copy(isSaved = true)
    }

    private fun changeHint() {
        analytics.recordEvent(CLICKED_PROMPT)
        val currentPromptNumber = _state.value.promptNumber
        val newPromptNumber = currentPromptNumber + 1
        _state.value = _state.value.copy(promptNumber = newPromptNumber)
    }

}