package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val searchRequest = MutableStateFlow(SearchRequest())

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchResults: Flow<PagingData<Entry>> = searchRequest
        .transformLatest { request ->
            if (request.debounce) {
                delay(300)
            }

            val trimmedQuery = request.query.trim()
            if (trimmedQuery.isEmpty()) {
                emit(PagingData.empty())
            } else {
                emitAll(repository.searchEntries(trimmedQuery))
            }
        }
        .cachedIn(viewModelScope)

    fun onSearchQueryChanged(queryString: String) {
        _uiState.update { it.copy(query = queryString) }
        searchRequest.value = SearchRequest(query = queryString, debounce = true)
    }

    fun onSearchTriggered(queryString: String) {
        _uiState.update { it.copy(query = queryString) }
        searchRequest.value = SearchRequest(query = queryString, debounce = false)
    }
}

data class SearchUiState(
    val query: String = ""
)

private data class SearchRequest(
    val query: String = "",
    val debounce: Boolean = false
)
