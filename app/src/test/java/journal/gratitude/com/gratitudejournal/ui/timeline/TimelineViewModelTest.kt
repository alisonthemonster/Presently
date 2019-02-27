package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import journal.gratitude.com.gratitudejournal.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import junit.framework.TestCase.assertEquals
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
    fun init_listWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntries() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val list = listOf(oldEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(list)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)

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
}
