package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    fun search(queryString: String): Flow<PagingData<Entry>> {
        return repository.searchEntries(queryString)
    }
}
