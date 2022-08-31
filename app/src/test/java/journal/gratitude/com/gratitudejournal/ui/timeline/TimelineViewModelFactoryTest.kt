package journal.gratitude.com.gratitudejournal.ui.timeline

import com.nhaarman.mockitokotlin2.mock
import junit.framework.TestCase
import org.junit.Test

class TimelineViewModelFactoryTest {

    private val factory = TimelineViewModelFactory(mock(), mock())

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(TimelineViewModel::class.java)
        val expected = TimelineViewModel(mock(), mock())

        TestCase.assertEquals(expected.getTimelineItems(), actual.getTimelineItems())
    }
}