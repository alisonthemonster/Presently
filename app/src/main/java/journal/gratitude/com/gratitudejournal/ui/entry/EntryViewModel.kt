package journal.gratitude.com.gratitudejournal.ui.entry

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.airbnb.mvrx.ViewModelContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.appComponent
import journal.gratitude.com.gratitudejournal.di.AssistedViewModelFactory
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class EntryViewModel @AssistedInject constructor(
    @Assisted initialState: EntryState,
    private val repository: EntryRepository
) : MavericksViewModel<EntryState>(initialState) {

    fun changePrompt() {
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
            }
        }
    }

    fun saveEntry() {
        withState {
            val entry = Entry(it.date, it.entryContent)
            viewModelScope.launch(Dispatchers.IO) {
                repository.addEntry(entry)
                setState {
                    copy(isSaved = true)
                }
            }
        }
    }

    fun setDate(passedInDate: String) {
        val date = passedInDate.toLocalDate()
        setState {
            copy(date = date)
        }
        viewModelScope.launch {
            val entry = repository.getEntry(date)
            setState {
                copy(entryContent = entry?.entryContent ?: "")
            }
        }
    }

    @AssistedFactory
    interface Factory: AssistedViewModelFactory<EntryViewModel, EntryState> {
        override fun create(state: EntryState): EntryViewModel
    }

    companion object : MavericksViewModelFactory<EntryViewModel, EntryState> {

        override fun initialState(viewModelContext: ViewModelContext): EntryState {
            val passedInDate = LocalDate.now() // TODO is there a way get from fragment args?
            val prompts = viewModelContext.activity.resources.getStringArray(R.array.prompts)
            prompts.shuffle() //randomise prompts
            val quote = viewModelContext.activity.resources.getStringArray(R.array.inspirations).random()
            return EntryState(passedInDate, "", null, quote, false, 0, prompts.toList(), false)
        }

        override fun create(viewModelContext: ViewModelContext, state: EntryState): EntryViewModel {
            val component = viewModelContext.activity.appComponent()
            val viewModelFactoryMap =component.viewModelFactories()
            val viewModelFactory = viewModelFactoryMap[EntryViewModel::class.java]

            @Suppress("UNCHECKED_CAST")
            val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<EntryViewModel, EntryState>
            return castedViewModelFactory?.create(state)!! //TODO
        }
    }
}