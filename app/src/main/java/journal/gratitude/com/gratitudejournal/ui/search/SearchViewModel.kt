package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow(PagingData.empty<Entry>())
    val searchResults: StateFlow<PagingData<Entry>> = _searchResults.asStateFlow()

    private val _searchRequests = MutableSharedFlow<SearchRequest>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val searchRequests = _searchRequests.asSharedFlow()

    init {
        observeSearchRequests()
    }

    private fun observeSearchRequests() {
        viewModelScope.launch {
            searchRequests.collectLatest { request ->
                if (request.debounce) {
                    delay(300)
                }
                performSearch(request.query)
            }
        }
    }

    private suspend fun performSearch(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            _searchResults.value = PagingData.empty()
            return
        }

        repository.searchEntries(trimmedQuery).collectLatest { pagingData ->
            _searchResults.value = pagingData
        }
    }

    fun onSearchQueryChanged(queryString: String) {
        _uiState.update { it.copy(query = queryString) }
        _searchRequests.tryEmit(SearchRequest(query = queryString, debounce = true))
    }

    fun onSearchTriggered(queryString: String) {
        _uiState.update { it.copy(query = queryString) }
        _searchRequests.tryEmit(SearchRequest(query = queryString, debounce = false))
    }
}

data class SearchUiState(
    val query: String = ""
)

private data class SearchRequest(
    val query: String,
    val debounce: Boolean
)
