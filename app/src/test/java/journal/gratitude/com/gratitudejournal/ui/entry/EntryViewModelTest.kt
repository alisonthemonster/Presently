package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.airbnb.mvrx.withState
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import com.airbnb.mvrx.test.MvRxTestRule
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.model.Entry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.threeten.bp.LocalDate

class EntryViewModelTest {

    private lateinit var viewModel: EntryViewModel

    private val repository = object : EntryRepository {
        override suspend fun getEntry(date: LocalDate): Entry? {
            return Entry(date, "hii there")
        }

        override suspend fun getEntriesFlow(): Flow<List<Entry>> {
            return flowOf(listOf(Entry(LocalDate.of(2021, 2, 28), "hii there")))
        }

        override suspend fun getEntries(): List<Entry> {
            return listOf(Entry(LocalDate.of(2021, 2, 28), "hii there"))
        }

        override fun getWrittenDates(): LiveData<List<LocalDate>> {
            return MutableLiveData(listOf(LocalDate.of(2021, 2, 28)))
        }

        override suspend fun addEntry(entry: Entry) = Unit

        override suspend fun addEntries(entries: List<Entry>) = Unit

        override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
    }
    private val application = mock<Application>()

    @get:Rule
    val mvrxRule = MvRxTestRule()

    @Before
    fun before() {
        whenever(application.resources).thenReturn(mock())
        whenever(application.resources.getStringArray(anyInt())).thenReturn(arrayOf("InspirationalQuote"))
    }

    @Test
    fun `GIVEN entry view model WHEN changePrompt is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", null, "quote", false, 0, listOf("one", "two"), false)
        viewModel = EntryViewModel(initialState, repository)
        viewModel.changePrompt()

        withState(viewModel) {
            assertEquals(it.promptNumber, 1)
            assertEquals(it.hint, "two")
        }
    }

    @Test
    fun `GIVEN entry view model WHEN onTextChanged is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", null, "quote", false, 0, listOf("one", "two"), false)
        viewModel = EntryViewModel(initialState, repository)
        viewModel.onTextChanged("new text")

        withState(viewModel) {
            assertEquals(it.entryContent, "new text")
            assertEquals(it.hasUserEdits, true)
        }
    }

    @Test
    fun `GIVEN entry view model WHEN setDate is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", null, "quote", false, 0, listOf("one", "two"), false)
        viewModel = EntryViewModel(initialState, repository)
        viewModel.setDate("2021-03-02")

        withState(viewModel) {
            assertEquals(it.date, LocalDate.of(2021, 3, 2))
            assertEquals(it.entryContent, "hii there")
        }
    }
}
