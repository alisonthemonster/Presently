package journal.gratitude.com.gratitudejournal.ui.search

import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase
import org.junit.Test

class SearchViewModelFactoryTest {

    private val factory = SearchViewModelFactory(mock())

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(SearchViewModel::class.java)

        TestCase.assertTrue(actual is SearchViewModel)
    }
}