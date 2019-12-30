package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.*
import journal.gratitude.com.gratitudejournal.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.threeten.bp.LocalDate
import java.io.IOException
import kotlin.test.assertFailsWith

class EntryViewModelTest {

    private lateinit var viewModel: EntryViewModel

    private val today = LocalDate.of(2011, 11, 11)
    private val todayString = today.toString()

    private val repository = mock<EntryRepository>()
    private val application = mock<Application>()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun before() {
        whenever(repository.getEntry(any())).thenReturn(mock())
        whenever(application.resources).thenReturn(mock())
        whenever(application.resources.getStringArray(anyInt())).thenReturn(arrayOf("InspirationalQuote"))
    }

    @Test
    fun initViewModel_CallsRepository_getEntry() {
        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(todayString)

        LiveDataTestUtil.getValue(viewModel.entry)

        verify(repository, times(1)).getEntry(any())
    }

    @Test
    fun initViewModel_CallsRepository_getEntry_withDate() {
        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(todayString)

        LiveDataTestUtil.getValue(viewModel.entry)

        verify(repository, times(1)).getEntry(todayString.toLocalDate())
    }

    @Test
    fun initViewModel_setsContentString() {
        val expectedContent = "Hello there friend!"
        val liveData = MutableLiveData<Entry>()
        liveData.postValue(Entry(LocalDate.now(), expectedContent))

        whenever(repository.getEntry(any())).thenReturn(liveData)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(todayString)
        LiveDataTestUtil.getValue(viewModel.entry)

        assertEquals(expectedContent, viewModel.entryContent.get())
    }

    @Test
    fun getDateString_Today_returnsToday() {
        val expected = "Today"
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getHintString_Today_returnsPresentTense() {
        val expected = "What are you grateful for?"
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())

        assertEquals(expected, viewModel.getHintString())
        verify(application.resources).getString(R.string.what_are_you_thankful_for)

    }

    @Test
    fun getHintString_Past_returnsPastTense() {
        val expected = "What were you grateful for?"
        val yesterday = LocalDate.now().minusDays(1)
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(yesterday.toString())

        assertEquals(expected, viewModel.getHintString())
        verify(application.resources).getString(R.string.what_were_you_thankful_for)
    }

    @Test
    fun getHintString_withNewHint() {
        val expected = arrayOf("prompt one")
        val yesterday = LocalDate.now().minusDays(1)
        whenever(application.resources.getStringArray(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(yesterday.toString())

        viewModel.getRandomPromptHintString()

        assertEquals(expected[0], viewModel.getHintString())
        verify(application.resources).getStringArray(R.array.prompts)
    }

    @Test
    fun getHintString_withQueueStyle() {
        val expected = arrayOf("prompt one", "prompt two")
        val yesterday = LocalDate.now().minusDays(1)
        whenever(application.resources.getStringArray(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(yesterday.toString())

        viewModel.getRandomPromptHintString()
        val first = viewModel.getHintString()
        viewModel.getRandomPromptHintString()
        val second = viewModel.getHintString()

        assert(first != second)
    }

    @Test
    fun getHintString_withPromptRecycling() {
        val expected = arrayOf("prompt one", "prompt two")
        val yesterday = LocalDate.now().minusDays(1)
        whenever(application.resources.getStringArray(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(yesterday.toString())

        viewModel.getRandomPromptHintString()
        val first = viewModel.getHintString()
        viewModel.getRandomPromptHintString()
        viewModel.getRandomPromptHintString()
        val third = viewModel.getHintString()

        assertEquals(first, third)
    }

    @Test
    fun getDateString_Yesterday_returnsYesterday() {
        val expected = "Yesterday"
        val yesterday = LocalDate.now().minusDays(1)
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(yesterday.toString())

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun deletingEntryContent_updatesIsEmpty() {
        viewModel = EntryViewModel(repository, application)

        viewModel.entryContent.set("hellooooo") //write content
        viewModel.entryContent.set("") //empty the contents

        assertEquals(true, viewModel.isEmpty.get())
    }

    @Test
    fun getDateString_OldDate_returnsOldDate() {
        val expected = "November 11, 2011"

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(todayString)

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getThankfulString_Today_returnsPresentTense() {
        val expected = "I am grateful for"
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())
        assertEquals(expected, viewModel.getThankfulString())
    }

    @Test
    fun getThankfulString_PastDay_returnsPastTense() {
        val expected = "I was grateful for"
        whenever(application.resources.getString(anyInt())).thenReturn(expected)

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(todayString)

        assertEquals(expected, viewModel.getThankfulString())
    }

    @Test
    fun getShareString_returnsShareString() {
        val expectedContent = "My dear friends"
        val liveData = MutableLiveData<Entry>()
        liveData.postValue(Entry(LocalDate.now(), expectedContent))
        val expectedPhrase = "I am grateful for"
        whenever(application.resources.getString(R.string.today)).thenReturn("Today")
        whenever(application.resources.getString(R.string.iam)).thenReturn(expectedPhrase)
        whenever(repository.getEntry(any())).thenReturn(liveData)

        val expected = "Today I am grateful for my dear friends"

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())
        LiveDataTestUtil.getValue(viewModel.entry)

        assertEquals(expected, viewModel.getShareContent())
    }

    @Test
    fun getShareStringEmptyContent_returnsShareString() {
        val expectedContent = ""
        val liveData = MutableLiveData<Entry>()
        liveData.postValue(Entry(LocalDate.now(), expectedContent))
        val expectedPhrase = "I am grateful for"
        whenever(application.resources.getString(R.string.today)).thenReturn("Today")
        whenever(application.resources.getString(R.string.iam)).thenReturn(expectedPhrase)
        whenever(repository.getEntry(any())).thenReturn(liveData)

        val expected = "Today I am grateful for "

        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())
        LiveDataTestUtil.getValue(viewModel.entry)

        assertEquals(expected, viewModel.getShareContent())
    }

    @Test
    fun getInspirationalQuote_returnsQuote() {
        viewModel = EntryViewModel(repository, application)
        viewModel.setDate(LocalDate.now().toString())

        assertEquals("InspirationalQuote", viewModel.getInspirationString())
    }

    @Test
    fun getDateString_noDate_throwsException() {
        viewModel = EntryViewModel(repository, application)

        assertFailsWith<IOException> {
            viewModel.getDateString()
        }
    }

    @Test
    fun getHintString_noDate_throwsException() {
        viewModel = EntryViewModel(repository, application)

        assertFailsWith<IOException> {
            viewModel.getHintString()
        }
    }

    @Test
    fun getThankfulString_noDate_throwsException() {
        viewModel = EntryViewModel(repository, application)

        assertFailsWith<IOException> {
            viewModel.getThankfulString()
        }
    }
}
