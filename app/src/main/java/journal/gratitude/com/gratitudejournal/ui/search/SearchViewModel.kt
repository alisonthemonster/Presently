package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.model.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val localSource: PresentlyLocalSource) : ViewModel() {

    fun search(queryString: String): Flow<PagingData<Entry>> {
        return localSource.searchEntries(queryString)
    }
}
