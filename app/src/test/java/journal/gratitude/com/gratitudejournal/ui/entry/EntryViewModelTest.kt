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
import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.verify
import com.presently.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.Entry
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
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
    private val analytics = object : AnalyticsLogger {
        override fun recordEvent(event: String) {}

        override fun recordEvent(event: String, details: Map<String, Any>) {}

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

        override fun recordEntryAdded(numEntries: Int)  {}

        override fun recordView(viewName: String) {}
    }

    @get:Rule
    val mvrxRule = MvRxTestRule()

    @Test
    fun `GIVEN entry view model WHEN changePrompt is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.changePrompt()

        withState(viewModel) {
            assertEquals(it.promptNumber, 1)
            assertEquals(it.hint, "two")
        }
    }

    @Test
    fun `GIVEN entry view model WHEN changePrompt is called THEN an analytics event is logged`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        var recordEventWasCalled = false
        var eventName = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordEventWasCalled = true
                eventName = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int)  {}

            override fun recordView(viewName: String) {}
        }

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.changePrompt()

        assertThat(eventName).isEqualTo("clickedNewPrompt")
        assertThat(recordEventWasCalled).isTrue()
    }

    @Test
    fun `GIVEN entry view model WHEN onTextChanged is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.onTextChanged("new text")

        withState(viewModel) {
            assertEquals(it.entryContent, "new text")
            assertEquals(it.hasUserEdits, true)
        }
    }

    @Test
    fun `GIVEN entry view model WHEN onCreate is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        var viewScreenWasCalled = false
        var screenName = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {}

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int)  {}

            override fun recordView(viewName: String) {
                viewScreenWasCalled = true
                screenName = viewName
            }
        }

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.onCreate()

        assertThat(viewScreenWasCalled).isTrue()
        assertThat(screenName).isEqualTo("EntryFragment")
    }

    @Test
    fun `GIVEN an entry view model WHEN the view model is created THEN the entry is fetched`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        var getEntryWasCalled = false
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? {
                getEntryWasCalled = true
                return Entry(date, "hii there")
            }

            override suspend fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override fun getWrittenDates(): LiveData<List<LocalDate>> = MutableLiveData(emptyList())

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        viewModel = EntryViewModel(initialState, analytics, repository)

        assertThat(getEntryWasCalled).isTrue()
    }

    @Test
    fun `GIVEN an entry view model WHEN saveEntry is called THEN the entry is added to the repository`() {
        val initialState = EntryState(LocalDate.now(), "", true, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        var addEntryWasCalled = false
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null

            override suspend fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override fun getWrittenDates(): LiveData<List<LocalDate>> = MutableLiveData(emptyList())

            override suspend fun addEntry(entry: Entry) {
                addEntryWasCalled = true
            }

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.saveEntry()

        assertThat(addEntryWasCalled).isTrue()
    }

    @Test
    fun `GIVEN an entry view model AND an existing entry WHEN saveEntry is called THEN an analytics event was logged`() {
        val initialState = EntryState(LocalDate.now(), "", false, null, "hint", "quote", false, 0, listOf("one", "two"), false)
        var recordEventWasCalled = false
        var eventName = ""
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {
                recordEventWasCalled = true
                eventName = event
            }

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(numEntries: Int)  {}

            override fun recordView(viewName: String) {}
        }

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.saveEntry()

        assertThat(recordEventWasCalled).isTrue()
        assertThat(eventName).isEqualTo("editedExistingEntry")
    }

    @Test
    fun `GIVEN an entry view model AND an new entry WHEN saveEntry is called THEN an analytics event was logged`() {
        val initialState = EntryState(LocalDate.now(), "", true, 2, "hint", "quote", false, 0, listOf("one", "two"), false)
        var recordEntryAddedWasCalled = false
        var numEntries = 0
        val analytics = object : AnalyticsLogger {
            override fun recordEvent(event: String) {}

            override fun recordEvent(event: String, details: Map<String, Any>) {}

            override fun recordSelectEvent(selectedContent: String, selectedContentType: String) {}

            override fun recordEntryAdded(num: Int)  {
                recordEntryAddedWasCalled = true
                numEntries = num
            }

            override fun recordView(viewName: String) {}
        }

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.saveEntry()

        assertThat(recordEntryAddedWasCalled).isTrue()
        assertThat(numEntries).isEqualTo(3)
    }

    @Test
    fun `GIVEN an entry view model AND an new entry AND 4 existing entries WHEN saveEntry is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", true, 4, "hint", "quote", false, 0, listOf("one", "two"), false)

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.saveEntry()

        withState(viewModel) {
            assertThat(it.milestoneNumber).isEqualTo(5)
        }
    }

    @Test
    fun `GIVEN an entry view model AND an existing entry WHEN saveEntry is called THEN the state is updated`() {
        val initialState = EntryState(LocalDate.now(), "", false, 4, "hint", "quote", false, 0, listOf("one", "two"), false)

        viewModel = EntryViewModel(initialState, analytics, repository)
        viewModel.saveEntry()

        withState(viewModel) {
            assertThat(it.isSaved).isTrue()
        }
    }

    @Test
    fun `GIVEN EntryArgs AND a new entry WHEN the viewModel is created THEN the initial state is set`() {
        val entryArgs = EntryArgs(LocalDate.now().toString(), true, 0, "Quote", "What are you grateful for?", listOf("one", "two", "three"))
        val initialState = EntryState(entryArgs)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null

            override suspend fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override fun getWrittenDates(): LiveData<List<LocalDate>> = MutableLiveData(emptyList())

            override suspend fun addEntry(entry: Entry) {}

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        viewModel = EntryViewModel(initialState, analytics, repository)

        withState(viewModel) {
            assertThat(it.date).isEqualTo(LocalDate.now())
            assertThat(it.entryContent).isEqualTo("")
            assertThat(it.isNewEntry).isTrue()
            assertThat(it.numberExistingEntries).isEqualTo(0)
            assertThat(it.hint).isEqualTo("What are you grateful for?")
            assertThat(it.quote).isEqualTo("Quote")
            assertThat(it.hasUserEdits).isFalse()
            assertThat(it.promptNumber).isEqualTo(0)
            assertThat(it.promptsList).isEqualTo(listOf("one", "two", "three"))
            assertThat(it.isSaved).isFalse()
            assertThat(it.milestoneNumber).isEqualTo(0)
        }
    }

    @Test
    fun `GIVEN EntryArgs AND a existing entry WHEN the viewModel is created THEN the initial state is set`() {
        val entryArgs = EntryArgs(LocalDate.now().toString(), false, 0, "Quote", "What are you grateful for?", listOf("one", "two", "three"))
        val initialState = EntryState(entryArgs)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null

            override suspend fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override fun getWrittenDates(): LiveData<List<LocalDate>> = MutableLiveData(emptyList())

            override suspend fun addEntry(entry: Entry) {}

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }
        viewModel = EntryViewModel(initialState, analytics, repository)

        withState(viewModel) {
            assertThat(it.date).isEqualTo(LocalDate.now())
            assertThat(it.entryContent).isEqualTo(" ")
            assertThat(it.isNewEntry).isFalse()
            assertThat(it.numberExistingEntries).isEqualTo(0)
            assertThat(it.hint).isEqualTo("What are you grateful for?")
            assertThat(it.quote).isEqualTo("Quote")
            assertThat(it.hasUserEdits).isFalse()
            assertThat(it.promptNumber).isEqualTo(0)
            assertThat(it.promptsList).isEqualTo(listOf("one", "two", "three"))
            assertThat(it.isSaved).isFalse()
            assertThat(it.milestoneNumber).isEqualTo(0)
        }
    }

    //TODO add tests for the arguments
}
