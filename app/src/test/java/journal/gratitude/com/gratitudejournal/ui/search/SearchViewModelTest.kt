package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.search

import android.view.View
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagedList
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.util.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.search.SearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

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
        LiveDataTestUtil.getValue(viewModel.results) //observe entries

        viewModel.search("Yo yo yo")

        verify(repository, times(1)).searchEntries(any())
    }

    @Test
    fun search_calls_repository_withQueryString() {
        LiveDataTestUtil.getValue(viewModel.results) //observe entries

        val queryString = "Yo yo yo"
        viewModel.search(queryString)

        verify(repository).searchEntries(queryString)
    }

    @Test
    fun noSearchResults_noResultsContentIsVisible() {
        val expectedLiveData = MutableLiveData<PagedList<Entry>>()
        expectedLiveData.postValue(mockPagedList(emptyList()))
        whenever(repository.searchEntries(any())).thenReturn(expectedLiveData)

        viewModel = SearchViewModel(repository)

        LiveDataTestUtil.getValue(viewModel.results) //observe entries

        viewModel.search("blerg")

        assertEquals(viewModel.noResults(), View.VISIBLE)
    }

    @Test
    fun searchResults_noResultsContentIsGone() {
        val expectedLiveData = MutableLiveData<PagedList<Entry>>()
        expectedLiveData.postValue(mockPagedList(listOf(Entry(LocalDate.now(), "blargle"))))
        whenever(repository.searchEntries(any())).thenReturn(expectedLiveData)

        viewModel = SearchViewModel(repository)

        LiveDataTestUtil.getValue(viewModel.results) //observe entries

        viewModel.search("blerg")

        assertEquals(viewModel.noResults(), View.GONE)
    }

    fun <T> mockPagedList(list: List<T>): PagedList<T> {
        val pagedList = mock<PagedList<T>>()
        whenever(pagedList[any()]).then { invocation ->
            val index = invocation.arguments.first() as Int
            list[index]
        }
        whenever(pagedList.size).thenReturn(list.size)
        return pagedList
    }

}