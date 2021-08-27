package journal.gratitude.com.gratitudejournal.ui.timeline

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import com.presently.presently_local_source.PresentlyLocalSource
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test


class TimelineViewModelFactoryTest {

    private val localSource = mock<PresentlyLocalSource>()
    private val factory = TimelineViewModelFactory(localSource)

    @Test
    fun create_createsAViewModel() = runBlockingTest {
        val actual = factory.create(TimelineViewModel::class.java)
        val expected = TimelineViewModel(localSource)

       assertThat(actual.getDatesWritten()).isEqualTo(expected.getDatesWritten())
    }
}