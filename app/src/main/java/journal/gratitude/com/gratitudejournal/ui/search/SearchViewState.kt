package journal.gratitude.com.gratitudejournal.ui.search

import journal.gratitude.com.gratitudejournal.model.Entry

data class SearchViewState(
    val query: String = "",
    val results: List<Entry> = emptyList(),
)
