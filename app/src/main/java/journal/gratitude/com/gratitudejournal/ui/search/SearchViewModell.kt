package journal.gratitude.com.gratitudejournal.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModell @Inject constructor(
    private val repository: EntryRepository,
    private val settings: PresentlySettings,
    private val analytics: AnalyticsLogger
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val _state = MutableStateFlow(SearchViewState())
    val state: StateFlow<SearchViewState> = _state

    init {
        viewModelScope.launch {
            searchQuery.debounce(300)
                .onEach { query ->
                    _state.value = _state.value.copy(
                        query = query,
                        results = repository.search(query)
                    )
                }
                .collect()
        }
    }

    fun search(searchTerm: String) {
        searchQuery.value = searchTerm
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun logScreenView() {
        analytics.recordView("Search")
    }
}
