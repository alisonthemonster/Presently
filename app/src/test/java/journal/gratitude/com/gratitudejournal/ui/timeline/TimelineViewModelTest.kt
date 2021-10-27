package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import journal.gratitude.com.gratitudejournal.util.LiveDataTestUtil
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class TimelineViewModelTest {

    private val repository = mock<EntryRepository>()

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @Test
    fun init_emptyList_returnListWithTodayEntry() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val expectedLiveData = MutableLiveData<List<Entry>>()
        expectedLiveData.postValue(emptyList())

        val mockFlow = flow {
            emit(emptyList<Entry>())
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }
        val viewModel = TimelineViewModel(repository)
        val actual = LiveDataTestUtil.getValue(viewModel.entries)

        assertEquals(listOf(todayEntry, yesterdayEntry), actual)
    }

    @Test
    fun init_emptylistWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntries() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val mockFlow = flow {
            emit(emptyList<Entry>())
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val expectedList = listOf(todayEntry, yesterdayEntry)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntriesToList() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val oldEntry1 = Entry(LocalDate.of(2011, 11, 10), "")
        val oldEntry2 = Entry(LocalDate.of(2011, 11, 9), "")

        val list = listOf(oldEntry, oldEntry1, oldEntry2)
        val mockFlow = flow {
            emit(list)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry, oldEntry1, oldEntry2)

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayAndYesterdayWritten_returnsOriginalList() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsOriginalListPlusYesterday() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsTodayPlusYesterday() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsOriginalListPlusToday() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsYesterdayPlusToday() {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntries_returnsFiveEntriesAndMilestone() {
        val todayEntry = Entry(LocalDate.now(), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(LocalDate.now().minusDays(i), "content"))
        }
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenThreeDaysAgo_returnsFiveEntriesAndMilestoneAndHints() {
        val firstEntry = Entry(LocalDate.now().minusDays(3), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(firstEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(LocalDate.now().minusDays(3 + i), "content"))
        }

        //empty entries for user to fill in today and yesterday
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHint() {
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()

        val pastDays = mutableListOf<Entry>()
        for (i in 0L until 5L) {
            pastDays.add(Entry(LocalDate.now().minusDays(1 + i), "content"))
        }
        writtenDates.addAll(pastDays)

        //empty entry for user to fill in today
        val todayEntry = Entry(LocalDate.now(), "")

        expectedList.add(todayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(pastDays)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenWithGapYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() {
        //the milestone entry is written today and yesterday was not filled in
        //the milestone should appear before today's entry which is followed by a hint

        val todayEntry = Entry(LocalDate.now(), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        val pastDays = mutableListOf<Entry>()
        for (i in 1L until 5L) {
            pastDays.add(Entry(LocalDate.now().minusDays(5 + i), "content"))
        }
        writtenDates.addAll(pastDays)

        //empty entry for user to fill in yesterday
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        expectedList.add(Milestone.create(5))
        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.addAll(pastDays)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithMilestoneWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() {
        //the milestone entry is written yesterday and today is not filled in
        //the milestone should appear after today's entry and then followed by a hint

        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(yesterdayEntry)
        val pastDays = mutableListOf<Entry>()
        for (i in 1L until 5L) {
            //write four entries in the past
            pastDays.add(Entry(LocalDate.now().minusDays(10 + i), "content"))
        }
        writtenDates.addAll(pastDays)

        //empty entry for user to fill in today
        val todayEntry = Entry(LocalDate.now(), "")

        expectedList.add(todayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.add(yesterdayEntry)
        expectedList.addAll(pastDays)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithMultipleMilestones_returnsCorrectOrderList() {
        //multiple milestones written in the past

        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        val pastDays = mutableListOf<Entry>()
        for (i in 5L until 10L) {
            //write five entries in the past
            pastDays.add(Entry(LocalDate.now().minusDays(i), "content"))
        }
        val morePastDays = mutableListOf<Entry>()
        for (i in 10L until 15L) {
            //write five entries in the more distant past
            morePastDays.add(Entry(LocalDate.now().minusDays(10 + i), "content"))
        }
        writtenDates.addAll(pastDays)
        writtenDates.addAll(morePastDays)

        //empty entries for user to fill in today and yesterday
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.add(Milestone.create(10))
        expectedList.addAll(pastDays)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(morePastDays)

        val mockFlow = flow {
            emit(writtenDates)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)

        val actual = LiveDataTestUtil.getValue(viewModel.entries)
        assertEquals(expectedList, actual)
    }

    @Test
    fun getEntriesList_returnsEntries() {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val mockFlow = flow {
            emit(expectedList)
        }
        runBlocking {
            whenever(repository.getEntriesFlow()).thenReturn(mockFlow)
        }

        val viewModel = TimelineViewModel(repository)
        LiveDataTestUtil.getValue(viewModel.entries) //observe entries

        val actual = viewModel.getTimelineItems()

        assertEquals(expectedList, actual)
    }

    @Test
    fun init_callsGetWrittenDates() {
        TimelineViewModel(repository)

        verify(repository, times(1)).getWrittenDates()
    }
}
