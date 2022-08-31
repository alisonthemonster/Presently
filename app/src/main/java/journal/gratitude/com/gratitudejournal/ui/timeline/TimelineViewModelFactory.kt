package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.presently.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
        private val repository: EntryRepository,
        private val dispatchers: AppCoroutineDispatchers
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimelineViewModel(repository, dispatchers) as T
    }
}
