package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.search

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.search.SearchEntryViewModel
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class SearchEntryViewModelTest {

    lateinit var viewModel: SearchEntryViewModel
    private val expectedYear = 2019
    private val expectedMonth = 11
    private val expectedMonthShort = "Nov"
    private val expectedDay = 29
    private val expectedContent = "Happy birthday Alison!"
    private val onClickListener = mock<TimelineAdapter.OnClickListener>()
    private val expectedDate = LocalDate.of(expectedYear, expectedMonth, expectedDay)

    private val entry = Entry(expectedDate, expectedContent)

    @Before
    fun before() {
        viewModel = SearchEntryViewModel(entry, onClickListener)
    }

    @Test
    fun getDay_returnsTheDayOfTheMonth() {
        assertEquals(expectedDay.toString(), viewModel.getDay())
    }

    @Test
    fun getMonth_returnsTheMonthShortened() {
        assertEquals(expectedMonthShort, viewModel.getMonth())
    }

    @Test
    fun getYear_returnsTheYear() {
        assertEquals(expectedYear.toString(), viewModel.getYear())
    }

    @Test
    fun onClick_callsTheClickListener() {
        viewModel.onClick(mock())

        verify(onClickListener, times(1)).onClick(expectedDate)
    }

}