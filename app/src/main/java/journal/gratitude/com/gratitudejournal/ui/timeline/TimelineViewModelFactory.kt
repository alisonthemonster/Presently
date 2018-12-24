package journal.gratitude.com.gratitudejournal.ui.timeline

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
        private val repository: EntryRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimelineViewModel(repository) as T
    }
}
