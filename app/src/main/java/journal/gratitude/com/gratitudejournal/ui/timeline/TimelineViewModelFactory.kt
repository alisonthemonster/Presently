package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.presently.presently_local_source.PresentlyLocalSource

@Suppress("UNCHECKED_CAST")
class TimelineViewModelFactory(
        private val presentlyLocalSource: PresentlyLocalSource
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimelineViewModel(presentlyLocalSource) as T
    }
}
