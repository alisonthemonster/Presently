package journal.gratitude.com.gratitudejournal.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    private val repository = mock<EntryRepository>()
    private lateinit var viewModel: SearchViewModel

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun before() {
        whenever(repository.searchEntries(any())).thenReturn(mock())

        viewModel = SearchViewModel(repository)
    }

    @Test
    fun search_calls_repository() {
        viewModel.search("Yo yo yo")

        verify(repository, times(1)).searchEntries("Yo yo yo")
    }

}