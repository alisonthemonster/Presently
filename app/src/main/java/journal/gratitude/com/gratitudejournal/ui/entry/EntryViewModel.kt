package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.model.COPIED_QUOTE
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.SHARED_ENTRY
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analytics: AnalyticsLogger,
    private val repository: EntryRepository,
    settings: PresentlySettings
) : ViewModel() {

    private val args = requireNotNull(savedStateHandle.get<EntryArgs>(EntryFragment.ENTRY_ARGS_KEY)) {
        "EntryArgs are required to open EntryFragment"
    }

    private val _state = MutableStateFlow(
        EntryUiState(args).copy(showQuote = settings.shouldShowQuote())
    )
    val state: StateFlow<EntryUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<EntryEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<EntryEffect> = _effects.asSharedFlow()

    private var originalEntryContent = ""
    private var screenViewRecorded = false

    init {
        reloadEntry()
    }

    fun onScreenShown() {
        if (screenViewRecorded) return
        screenViewRecorded = true
        analytics.recordView("EntryFragment")
    }

    fun onPromptClicked() {
        analytics.recordEvent(CLICKED_PROMPT)
        _state.update { current ->
            if (current.promptsList.isEmpty()) {
                current
            } else {
                val newPromptNumber =
                    if (current.promptNumber < current.promptsList.lastIndex) current.promptNumber + 1 else 0
                current.copy(
                    hint = current.promptsList[newPromptNumber],
                    promptNumber = newPromptNumber
                )
            }
        }
    }

    fun onTextChanged(newText: String) {
        _state.update {
            it.copy(
                entryContent = newText,
                hasUnsavedChanges = calculateHasUnsavedChanges(
                    currentText = newText,
                    isNewEntry = it.isNewEntry
                )
            )
        }
    }

    fun onShareClicked() {
        val currentState = _state.value
        if (currentState.entryContent.isEmpty()) return

        analytics.recordEvent(SHARED_ENTRY)
        _effects.tryEmit(
            EntryEffect.OpenShare(
                entryContent = currentState.entryContent,
                date = currentState.date
            )
        )
    }

    fun onQuoteLongClicked() {
        analytics.recordEvent(COPIED_QUOTE)
        _effects.tryEmit(EntryEffect.CopyQuote(_state.value.quote))
    }

    fun onBackPressed() {
        requestExit()
    }

    fun onDiscardChangesConfirmed() {
        _effects.tryEmit(EntryEffect.NavigateBack)
    }

    fun saveEntry() {
        val currentState = _state.value
        val entry = Entry(currentState.date, currentState.entryContent)

        viewModelScope.launch {
            repository.addEntry(entry)
            originalEntryContent = entry.entryContent

            _state.update {
                it.copy(
                    entryContent = entry.entryContent,
                    hasUnsavedChanges = false
                )
            }

            val shouldTriggerReminderOnboarding =
                currentState.isNewEntry && (currentState.numberExistingEntries ?: -1) == 0

            if (currentState.isNewEntry) {
                val totalEntries = (currentState.numberExistingEntries ?: 0) + 1
                analytics.recordEntryAdded(totalEntries)
                _effects.emit(
                    EntryEffect.EntrySaved(
                        milestoneNumber = if (isMilestone(totalEntries)) totalEntries else 0,
                        shouldTriggerReminderOnboarding = shouldTriggerReminderOnboarding
                    )
                )
            } else {
                analytics.recordEvent(EDITED_EXISTING_ENTRY)
                _effects.emit(
                    EntryEffect.EntrySaved(
                        milestoneNumber = 0,
                        shouldTriggerReminderOnboarding = false
                    )
                )
            }
        }
    }

    fun reloadEntry() {
        val date = _state.value.date
        _state.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val savedEntry = repository.getEntry(date)?.entryContent.orEmpty()
            originalEntryContent = savedEntry
            _state.update {
                it.copy(
                    entryContent = savedEntry,
                    isLoading = false,
                    hasUnsavedChanges = false
                )
            }
        }
    }

    private fun requestExit() {
        if (_state.value.hasUnsavedChanges) {
            _effects.tryEmit(EntryEffect.ShowUnsavedChangesDialog)
        } else {
            _effects.tryEmit(EntryEffect.NavigateBack)
        }
    }

    private fun calculateHasUnsavedChanges(currentText: String, isNewEntry: Boolean): Boolean {
        return currentText != originalEntryContent &&
            !(isNewEntry && currentText.isEmpty())
    }
}
