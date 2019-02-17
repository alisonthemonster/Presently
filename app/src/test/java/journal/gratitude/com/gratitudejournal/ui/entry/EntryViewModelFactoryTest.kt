package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers

class EntryViewModelFactoryTest {

    private val dateString = "2011-11-11"
    private val application = mock<Application>()
    private val factory = EntryViewModelFactory(dateString, mock(), application)

    @Before
    fun before() {
        whenever(application.resources).thenReturn(mock())
        whenever(application.resources.getStringArray(ArgumentMatchers.anyInt())).thenReturn(
            arrayOf(
                "InspirationalQuote"
            )
        )
        whenever(application.resources.getString(ArgumentMatchers.anyInt())).thenReturn("")
    }

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(EntryViewModel::class.java)
        val expected = EntryViewModel(dateString, mock(), application)

        assertEquals(expected.getThankfulString(), actual.getThankfulString())
    }
}