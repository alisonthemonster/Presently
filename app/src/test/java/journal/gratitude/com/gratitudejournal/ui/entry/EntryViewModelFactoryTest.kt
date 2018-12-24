package journal.gratitude.com.gratitudejournal.ui.entry

import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase.assertEquals
import org.junit.Test

class EntryViewModelFactoryTest {

    private val dateString = "2011-11-11"
    private val factory = EntryViewModelFactory(dateString, mock())

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(EntryViewModel::class.java)
        val expected = EntryViewModel(dateString, mock())

        assertEquals(expected.getThankfulString(), actual.getThankfulString())
    }
}