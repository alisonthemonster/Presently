package journal.gratitude.com.gratitudejournal.ui.entry

import junit.framework.TestCase.assertEquals
import org.junit.Test

class EntryViewModelFactoryTest {

    private val dateString = "2011-11-11"
    private val factory = EntryViewModelFactory(dateString)

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(EntryViewModel::class.java)
        val expected = EntryViewModel(dateString)

        assertEquals(expected.getThankfulString(), actual.getThankfulString())
    }
}