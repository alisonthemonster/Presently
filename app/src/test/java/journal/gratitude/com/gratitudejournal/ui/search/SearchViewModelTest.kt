package journal.gratitude.com.gratitudejournal.ui.search

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {

    private val repository = mock<EntryRepository>()
    private lateinit var viewModel: SearchViewModel

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun before() {
        whenever(repository.searchEntries(any())).thenReturn(flowOf(PagingData.empty()))

        viewModel = SearchViewModel(repository)
    }

    @Test
    fun onSearchQueryChanged_updatesUiState() = runTest {
        viewModel.onSearchQueryChanged("Yo yo yo")

        assertThat(viewModel.uiState.value.query).isEqualTo("Yo yo yo")
    }

    @Test
    fun searchResults_calls_repository_afterDebounce() = runTest() {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Yo yo yo")
        advanceTimeBy(299)
        verify(repository, never()).searchEntries(any())

        advanceTimeBy(1)
        advanceUntilIdle()
        verify(repository, times(1)).searchEntries("Yo yo yo")
    }

    @Test
    fun searchResults_doesNotSearchBlankStrings() = runTest() {
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("")
        advanceTimeBy(300)
        advanceUntilIdle()

        verify(repository, never()).searchEntries(any())
    }

    @Test
    fun onSearchTriggered_searchesImmediately() = runTest() {
        advanceUntilIdle()

        viewModel.onSearchTriggered("Yo yo yo")
        advanceUntilIdle()

        verify(repository, times(1)).searchEntries("Yo yo yo")
    }
}
