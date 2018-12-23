package journal.gratitude.com.gratitudejournal.ui.entry

import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDate

class EntryViewModelTest {

    private lateinit var viewModel: EntryViewModel

    private val today = LocalDate.of(2011, 11, 11)
    private val todayString = today.toString()

    @Before
    fun before() {
        viewModel = EntryViewModel(todayString)
    }

    @Test
    fun fetchEntryContent_setsSavedString_Saved() {
        val expected = "Saved"

        viewModel.fetchEntryContent()

        assertEquals(expected, viewModel.getSavingString())
    }

    @Test
    fun beforeFetchEntryContent_setsSavedString_Empty() {
        val expected = ""

        assertEquals(expected, viewModel.getSavingString())
    }

    @Test
    fun getDateString_Today_returnsToday() {
        val expected = "Today"

        viewModel = EntryViewModel(LocalDate.now().toString())
        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getDateString_Yesterday_returnsYesterday() {
        val expected = "Yesterday"
        val yesterday = LocalDate.now().minusDays(1)

        viewModel = EntryViewModel(yesterday.toString())
        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getDateString_OldDate_returnsOldDate() {
        val expected = "November 11, 2011"

        assertEquals(expected, viewModel.getDateString())
    }

    @Test
    fun getThankfulString_Today_returnsPresentTense() {
        val expected = "I am thankful for"

        viewModel = EntryViewModel(LocalDate.now().toString())
        assertEquals(expected, viewModel.getThankfulString())
    }

    @Test
    fun getThankfulString_PastDay_returnsPastTense() {
        val expected = "I was thankful for"

        assertEquals(expected, viewModel.getThankfulString())
    }

}
