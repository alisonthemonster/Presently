package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.presently.presently_local_source.PresentlyLocalSource

@Suppress("UNCHECKED_CAST")
class SearchViewModelFactory(
    private val localSource: PresentlyLocalSource
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SearchViewModel(localSource) as T
    }
}
