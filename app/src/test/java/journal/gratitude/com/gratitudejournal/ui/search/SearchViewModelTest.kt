package journal.gratitude.com.gratitudejournal.ui.search

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
        val collector = async { viewModel.searchResults.drop(1).first() }
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("Yo yo yo")
        advanceTimeBy(299)
        verify(repository, never()).searchEntries(any())

        advanceTimeBy(1)
        collector.await()
        verify(repository, times(1)).searchEntries("Yo yo yo")
    }

    @Test
    fun searchResults_doesNotSearchBlankStrings() = runTest() {
        val collector = async { viewModel.searchResults.drop(1).first() }
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("")
        advanceTimeBy(300)
        collector.await()

        verify(repository, never()).searchEntries(any())
    }

    @Test
    fun onSearchTriggered_searchesImmediately() = runTest() {
        val collector = async { viewModel.searchResults.drop(1).first() }
        advanceUntilIdle()

        viewModel.onSearchTriggered("Yo yo yo")
        collector.await()

        verify(repository, times(1)).searchEntries("Yo yo yo")
    }

    @Test
    fun searchResults_multipleCollectors_shareSingleSearch() = runTest {
        val firstCollector = async { viewModel.searchResults.drop(1).first() }
        val secondCollector = async { viewModel.searchResults.drop(1).first() }
        advanceUntilIdle()

        viewModel.onSearchTriggered("Yo yo yo")
        firstCollector.await()
        secondCollector.await()

        verify(repository, times(1)).searchEntries("Yo yo yo")
    }
}
