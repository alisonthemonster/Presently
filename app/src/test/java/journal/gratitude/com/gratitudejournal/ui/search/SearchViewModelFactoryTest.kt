package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.search

import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.ui.search.SearchViewModel
import journal.gratitude.com.gratitudejournal.ui.search.SearchViewModelFactory
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModel
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModelFactory
import junit.framework.TestCase
import org.junit.Test

class SearchViewModelFactoryTest {

    private val factory = SearchViewModelFactory(mock())

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(SearchViewModel::class.java)
        val expected = SearchViewModel(mock())

        TestCase.assertEquals(expected.noResults(), actual.noResults())
    }
}