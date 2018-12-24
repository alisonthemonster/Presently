package journal.gratitude.com.gratitudejournal.ui.entry

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class EntryViewModelTest {

    private lateinit var viewModel: EntryViewModel

    private val today = LocalDate.of(2011, 11, 11)
    private val todayString = today.toString()

    private val repository = mock<EntryRepository>()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun before() {
        whenever(repository.getEntry(any())).thenReturn(mock())
    }

    @Test
    fun initViewModel_CallsRepository_getEntry() {
        viewModel = EntryViewModel(todayString, repository)

        verify(repository, times(1)).getEntry(any())
    }

    @Test
    fun initViewModel_CallsRepository_getEntry_withDate() {
        viewModel = EntryViewModel(todayString, repository)

        verify(repository, times(1)).getEntry(todayString.toLocalDate())
    }

    @Test
    fun initViewModel_setsContentString() {
        val expectedContent = "Hello there friend!"
        val liveData = MutableLiveData<Entry>()
        liveData.postValue(Entry(LocalDate.now(), expectedContent))

        whenever(repository.getEntry(any())).thenReturn(liveData)

        viewModel = EntryViewModel(todayString, repository)
        LiveDataTestUtil.getValue(viewModel.entry)

        assertEquals(expectedContent, viewModel.entryContent.get())
    }

    @Test
    fun getDateString_Today_returnsToday() {
        val expected = "Today"

        viewModel = EntryViewModel(LocalDate.now().toString(), repository)

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getDateString_Yesterday_returnsYesterday() {
        val expected = "Yesterday"
        val yesterday = LocalDate.now().minusDays(1)

        viewModel = EntryViewModel(yesterday.toString(), repository)

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getDateString_OldDate_returnsOldDate() {
        val expected = "November 11, 2011"

        viewModel = EntryViewModel(todayString, repository)

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getThankfulString_Today_returnsPresentTense() {
        val expected = "I am thankful for"

        viewModel = EntryViewModel(LocalDate.now().toString(), repository)
        assertEquals(expected, viewModel.getThankfulString())
    }

    @Test
    fun getThankfulString_PastDay_returnsPastTense() {
        val expected = "I was thankful for"

        viewModel = EntryViewModel(todayString, repository)

        assertEquals(expected, viewModel.getThankfulString())
    }

}
