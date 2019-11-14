package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@Suppress("UNCHECKED_CAST")
class EntryViewModelFactory(
    private val repository: EntryRepository,
    private val application: Application
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EntryViewModel(repository, application) as T
    }
}
