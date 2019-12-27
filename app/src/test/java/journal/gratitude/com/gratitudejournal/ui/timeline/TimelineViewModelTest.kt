package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineViewModelTest {

    private val repository = mock<EntryRepository>()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Test
    fun init_calls_repository() {
        TimelineViewModel(repository)

        verify(repository, times(1)).getAllEntries()
    }

    @Test
    fun init_emptyList_returnListWithTodayEntry() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(emptyList())
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)
        val actual = LiveDataTestUtil.getValue(viewModel.entries)

        assertEquals(listOf(todayEntry, yesterdayEntry), actual)
    }

    @Test
    fun init_emptylistWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntries() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val list = emptyList<Entry>()
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(list)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val expectedList = listOf(todayEntry, yesterdayEntry)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntriesToList() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val oldEntry1 = Entry(LocalDate.of(2011, 11, 10), "")
        val oldEntry2 = Entry(LocalDate.of(2011, 11, 9), "")

        val list = listOf(oldEntry, oldEntry1, oldEntry2)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(list)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry, oldEntry1, oldEntry2)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayAndYesterdayWritten_returnsOriginalList() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(expectedList)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsOriginalListPlusYesterday() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(listOf(todayEntry, oldEntry))
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsTodayPlusYesterday() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(listOf(todayEntry))
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsOriginalListPlusToday() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(listOf(yesterdayEntry, oldEntry))
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsYesterdayPlusToday() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(listOf(yesterdayEntry))
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun getEntriesList_returnsEntries() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(expectedList)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)
        LiveDataTestUtil.getValue(viewModel.entries) //observe entries

        val actual = viewModel.getEntriesList()

        assertEquals(expectedList, actual)
    }

    @Test
    fun init_callsGetWrittenDates() {
        val viewModel = TimelineViewModel(repository)

        verify(repository, times(1)).getWrittenDates()
    }

    @Test
    fun addEntries_callsRepository() = runBlocking {
        val entries = listOf(Entry(LocalDate.now(), "test"))
        val viewModel = TimelineViewModel(repository)

        viewModel.addEntries(entries)

        verify(repository).addEntries(any())
    }
}
