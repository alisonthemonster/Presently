package journal.gratitude.com.gratitudejournal.ui.search

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class SearchViewModelTest {

    private var wasSearchEntriesCalled = false

    private val localSource = object : PresentlyLocalSource {
        override suspend fun getEntry(date: LocalDate): Entry {
            return Entry(date, "hii there")
        }

        override fun getEntriesFlow(): Flow<List<Entry>> {
            return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
        }

        override suspend fun getEntries(): List<Entry> {
            return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
        }

        override suspend fun getWrittenDates(): List<LocalDate> {
            return listOf(LocalDate.of(2021, 2, 28))
        }

        override suspend fun addEntry(entry: Entry) = Unit

        override suspend fun addEntries(entries: List<Entry>) = Unit

        override fun searchEntries(query: String): Flow<PagingData<Entry>> {
            wasSearchEntriesCalled = true
            return flowOf(PagingData.empty())
        }
    }

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()


    @Test
    fun search_calls_localsource() {
        wasSearchEntriesCalled = false
        val viewModel = SearchViewModel(localSource)
        viewModel.search("Yo yo yo")

        assertThat(wasSearchEntriesCalled).isTrue()
    }

}