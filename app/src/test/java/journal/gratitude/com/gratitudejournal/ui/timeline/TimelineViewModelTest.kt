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

        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(emptyList())
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)
        val actual = LiveDataTestUtil.getValue(viewModel.entries)

        assertEquals(listOf(todayEntry), actual)
    }

    @Test
    fun init_listWithoutTodayWritten_addsEmptyTodayEntry() {
        val todayEntry = Entry(LocalDate.now(), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val list = listOf(oldEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(list)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val expectedList = listOf(todayEntry, oldEntry)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWritten_returnsOriginalList() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val expectedList = listOf(todayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(expectedList)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun getEntriesList_returnsEntries() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val expectedList = listOf(todayEntry)
        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(expectedList)
        whenever(repository.getAllEntries()).thenReturn(expectedLiveData)

        val viewModel = TimelineViewModel(repository)
        LiveDataTestUtil.getValue(viewModel.entries) //observe entries

        val actual = viewModel.getEntriesList()

        assertEquals(expectedList, actual)
    }
}
