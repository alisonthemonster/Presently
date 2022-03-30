package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.logging.AnalyticsLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class EntryyViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val analytics: AnalyticsLogger,
) : ViewModel() {
    private val _state = MutableStateFlow(EntryViewState())
    val state: StateFlow<EntryViewState> = _state

    fun fetchContent(date: LocalDate) {
        viewModelScope.launch {
            val content = repository.getEntry(date)
            _state.value = _state.value.copy(
                date = date,
                content = content?.entryContent ?: ""
            )
        }
    }

    fun handleEvent(entryEvent: EntryEvent) {
        when (entryEvent) {
            EntryEvent.OnHintClicked -> changeHint()
            EntryEvent.OnSaveClicked -> saveEntry()
            EntryEvent.OnShareClicked -> TODO()
            is EntryEvent.OnTextChanged -> {
                _state.value = _state.value.copy(content = entryEvent.newText)
            }
        }
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
            val totalEntries = (_state.value.numberExistingEntries ?: 0) + 1
            analytics.recordEntryAdded(totalEntries)
            if (isMilestone(totalEntries)) {
                _state.value = _state.value.copy(milestoneNumber = totalEntries)
            }
        } else {
            analytics.recordEvent(EDITED_EXISTING_ENTRY)
        }
        _state.value = _state.value.copy(isSaved = true)
    }

    private fun changeHint() {
        analytics.recordEvent(CLICKED_PROMPT)
        val currentPromptNumber = _state.value.promptNumber
        val prompts = _state.value.promptsList
        val newPromptNumber =
            if (currentPromptNumber < prompts.size - 1) currentPromptNumber + 1 else 0
        _state.value = _state.value.copy(promptNumber = newPromptNumber)
    }

}