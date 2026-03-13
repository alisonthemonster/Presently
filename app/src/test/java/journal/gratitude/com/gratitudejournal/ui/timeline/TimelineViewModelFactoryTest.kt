package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.TestCase
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineViewModelFactoryTest {

    private val repository = mock<EntryRepository>()
    private val factory = TimelineViewModelFactory(repository)

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setUp() {
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(flowOf(emptyList()))
            whenever(repository.getWrittenDates()).thenReturn(MutableLiveData(emptyList<LocalDate>()))
        }
    }

    @Test
    fun create_createsAViewModel() {
        val actual = factory.create(TimelineViewModel::class.java)

        TestCase.assertTrue(actual is TimelineViewModel)
    }
}
