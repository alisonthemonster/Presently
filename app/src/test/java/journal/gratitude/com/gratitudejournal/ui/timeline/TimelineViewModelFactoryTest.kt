package journal.gratitude.com.gratitudejournal.journal.gratitude.com.gratitudejournal.ui.timeline

import com.nhaarman.mockitokotlin2.mock
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModel
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModelFactory
import junit.framework.TestCase
import org.junit.Test

class TimelineViewModelFactoryTest {

    private val factory = TimelineViewModelFactory(mock())

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(TimelineViewModel::class.java)
        val expected = TimelineViewModel(mock())

        TestCase.assertEquals(expected.getTimelineItems(), actual.getTimelineItems())
    }
}