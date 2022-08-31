package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.presently.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineViewModelTest {

    private val repository = object : EntryRepository {
        override suspend fun getEntry(date: LocalDate): Entry? = null
        override fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()
        override suspend fun getEntries(): List<Entry> = emptyList()
        override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
        override suspend fun addEntry(entry: Entry) {}
        override suspend fun addEntries(entries: List<Entry>) = Unit
        override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
    }

    private val dispatchers = AppCoroutineDispatchers(
        io = TestCoroutineDispatcher(),
        computation = TestCoroutineDispatcher(),
        main = TestCoroutineDispatcher()
    )

    @Test
    fun init_emptyList_returnListWithTodayEntry() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")

        val viewModel = TimelineViewModel(repository, dispatchers)
        val actual = viewModel.getTimelineItems().first()

        assertEquals(listOf(todayEntry, yesterdayEntry), actual)
    }

    @Test
    fun init_listWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntriesToList() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val oldEntry1 = Entry(LocalDate.of(2011, 11, 10), "")
        val oldEntry2 = Entry(LocalDate.of(2011, 11, 9), "")

        val list = listOf(oldEntry, oldEntry1, oldEntry2)

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(list)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry, oldEntry1, oldEntry2)

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayAndYesterdayWritten_returnsOriginalList() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(expectedList)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsOriginalListPlusYesterday() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(expectedList)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsTodayPlusYesterday() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(expectedList)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsOriginalListPlusToday() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(expectedList)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsYesterdayPlusToday() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(todayEntry, yesterdayEntry)
        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(expectedList)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntries_returnsFiveEntriesAndMilestone() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(LocalDate.now().minusDays(i), "content"))
        }
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates)

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenThreeDaysAgo_returnsFiveEntriesAndMilestoneAndHints() = runBlockingTest {
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

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHint() = runBlockingTest {
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

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithFiveEntriesWrittenWithGapYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() = runBlockingTest {
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

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithMilestoneWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() = runBlockingTest {
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

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_listWithMultipleMilestones_returnsCorrectOrderList() = runBlockingTest {
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

        val repository = object : EntryRepository {
            override suspend fun getEntry(date: LocalDate): Entry? = null
            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)
            override suspend fun getEntries(): List<Entry> = emptyList()
            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()
            override suspend fun addEntry(entry: Entry) {}
            override suspend fun addEntries(entries: List<Entry>) = Unit
            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(repository, dispatchers)

        val actual = viewModel.getTimelineItems().first()
        assertEquals(expectedList, actual)
    }

    @Test
    fun init_callsGetWrittenDates() = runBlockingTest {
        TimelineViewModel(repository, dispatchers)

        verify(repository, times(1)).getWrittenDates()
    }
}
