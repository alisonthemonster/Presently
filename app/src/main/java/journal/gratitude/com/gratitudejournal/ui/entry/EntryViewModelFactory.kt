package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@Suppress("UNCHECKED_CAST")
class EntryViewModelFactory(
    private val date: String,
    private val repository: EntryRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EntryViewModel(date, repository) as T
    }
}
