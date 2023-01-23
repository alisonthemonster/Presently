package journal.gratitude.com.gratitudejournal.ui.entry

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.presently.logging.AnalyticsLogger
import com.presently.mavericks_utils.AssistedViewModelFactory
import com.presently.mavericks_utils.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.launch

class EntryViewModel @AssistedInject constructor(
    @Assisted initialState: EntryState,
    private val analytics: AnalyticsLogger,
    private val repository: EntryRepository
) : MavericksViewModel<EntryState>(initialState) {

    init {
        getEntry()
    }

    fun getEntry() {
        withState {
            viewModelScope.launch {
                val entry = repository.getEntry(it.date)
                if (entry != null) {
                    setState {
                        copy(entryContent = entry.entryContent)
                    }
                } else {
                    setState { copy(entryContent = "", hasUserEdits = false) }
                }
            }
        }
    }

    fun onCreate() {
        analytics.recordView("EntryFragment")
    }

    fun changePrompt() {
        analytics.recordEvent(CLICKED_PROMPT)
        setState {
            val newPromptNumber = if (promptNumber < promptsList.size - 1) promptNumber + 1 else 0
            val newPrompt = promptsList[newPromptNumber]
            copy(hint = newPrompt, promptNumber = newPromptNumber)
        }
    }

    fun onTextChanged(newText: String) {
        withState { oldState ->
            if (oldState.entryContent != newText) {
                setState {
                    copy(entryContent = newText, hasUserEdits = true)
                }
            } else {
                setState {
                    copy(entryContent = newText, hasUserEdits = false)
                }
            }
        }
    }

    fun saveEntry() {
        withState {
            val entry = Entry(it.date, it.entryContent)
            viewModelScope.launch {
                repository.addEntry(entry)
            }
            if (it.isNewEntry) {
                val totalEntries = (it.numberExistingEntries ?: 0) + 1
                analytics.recordEntryAdded(totalEntries)
                if (isMilestone(totalEntries)) {
                    setState {
                        copy(milestoneNumber = totalEntries)
                    }
                } else {
                    setState {
                        copy(isSaved = true)
                    }
                }
            } else {
                analytics.recordEvent(EDITED_EXISTING_ENTRY)
                setState {
                    copy(isSaved = true)
                }
            }

        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<EntryViewModel, EntryState> {
        override fun create(state: EntryState): EntryViewModel
    }

    companion object :
        MavericksViewModelFactory<EntryViewModel, EntryState> by hiltMavericksViewModelFactory()

}