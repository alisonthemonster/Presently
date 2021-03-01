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
import java.util.*

class EntryMvrxViewModel @AssistedInject constructor(
    @Assisted initialState: EntryState,
    private val repository: EntryRepository
) : MavericksViewModel<EntryState>(initialState) {

    //TODO app crashes here "impure reducer"
        //because this can't run twice and return the same result
    fun changePrompt() {
        setState {
            val newPrompt = prompts.remove()
            prompts.offer(newPrompt)
            copy(hint = newPrompt, prompts = prompts)
        }
    }

    fun onTextChanged(newText: String) {
        setState {
            copy(entryContent = newText, hasUserEdits = true)
        }
    }

    fun saveEntry() {
        withState {
            val entry = Entry(it.date, it.entryContent)
            viewModelScope.launch(Dispatchers.IO) {
                repository.addEntry(entry)
            }
        }
    }

    fun setDate(passedInDate: String) {
        val date = passedInDate.toLocalDate()
        setState {
            copy(date = date)
        }
        viewModelScope.launch {
            val entry = repository.getEntrySuspend(date)
            setState {
                copy(entryContent = entry?.entryContent ?: "")
            }
        }
    }

    @AssistedFactory
    interface Factory: AssistedViewModelFactory<EntryMvrxViewModel, EntryState> {
        override fun create(state: EntryState): EntryMvrxViewModel
    }

    companion object : MavericksViewModelFactory<EntryMvrxViewModel, EntryState> {

        override fun initialState(viewModelContext: ViewModelContext): EntryState {
            val passedInDate = LocalDate.now() // TODO is there a way get from fragment args?
            val prompts = viewModelContext.activity.resources.getStringArray(R.array.prompts)
            val quote = viewModelContext.activity.resources.getStringArray(R.array.inspirations).random()
            val promptQueue = getPromptsQueue(prompts)
            val firstPrompt = promptQueue.remove()
            promptQueue.offer(firstPrompt)
            return EntryState(passedInDate, "", firstPrompt, quote, false, promptQueue)
        }

        override fun create(viewModelContext: ViewModelContext, state: EntryState): EntryMvrxViewModel {
            val component = viewModelContext.activity.appComponent()
            val viewModelFactoryMap =component.viewModelFactories()
            val viewModelFactory = viewModelFactoryMap[EntryMvrxViewModel::class.java]

            @Suppress("UNCHECKED_CAST")
            val castedViewModelFactory = viewModelFactory as? AssistedViewModelFactory<EntryMvrxViewModel, EntryState>
            return castedViewModelFactory?.create(state)!! //TODO
        }

        private fun getPromptsQueue(prompts: Array<String>): LinkedList<String> {
            val shuffled = prompts.toMutableList().shuffled()
            val queue = LinkedList<String>()
            for (prompt in shuffled) {
                queue.add(prompt)
            }
            return queue
        }
    }
}