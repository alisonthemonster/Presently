package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.model.Entry
import com.presently.testing.MainCoroutineRule
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineEntry
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate

class TimelineViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Test
    fun `GIVEN a user with no entries WHEN getTimelineItems is called THEN return a list with today and yesterday placeholders`() = runBlockingTest {
        val todayEntry = TimelineEntry(LocalDate.now(), "")
        val yesterdayEntry = TimelineEntry(LocalDate.now().minusDays(1), "")
        val expected = listOf(todayEntry, yesterdayEntry)

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(emptyList())

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }
        val viewModel = TimelineViewModel(localSource)
        val actual = viewModel.getTimelineItems().first()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a user with only entries older than yesterday WHEN getTimelineItems is called THEN return a list with today and yesterday added`() = runBlockingTest {
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "")
        val oldEntry1 = Entry(LocalDate.of(2011, 11, 10), "")
        val oldEntry2 = Entry(LocalDate.of(2011, 11, 9), "")

        val list = listOf(oldEntry, oldEntry1, oldEntry2)

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(list)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val expectedList = listOf(
            TimelineEntry(LocalDate.now(), ""),
            TimelineEntry(LocalDate.now().minusDays(1), ""),
            TimelineEntry(oldEntry.entryDate, oldEntry.entryContent),
            TimelineEntry(oldEntry1.entryDate, oldEntry1.entryContent),
            TimelineEntry(oldEntry2.entryDate, oldEntry2.entryContent)
        )

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with only entries for today and yesterday WHEN getTimelineItems is called THEN return the list with no placeholders`() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "howdy")
        val expectedList = listOf(TimelineEntry(todayEntry.entryDate, todayEntry.entryContent), TimelineEntry(yesterdayEntry.entryDate, yesterdayEntry.entryContent))

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(listOf(todayEntry, yesterdayEntry))

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with only an entry for today WHEN getTimelineItems is called THEN return today and a placeholder`() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val expectedList = listOf(TimelineEntry(todayEntry.entryDate, todayEntry.entryContent), TimelineEntry(LocalDate.now().minusDays(1), ""))

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(listOf(todayEntry))

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with only an entry for yesterday WHEN getTimelineItems is called THEN return yesterday and a placeholder`() = runBlockingTest {
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "")
        val expectedList = listOf(TimelineEntry(LocalDate.now(), ""), TimelineEntry(yesterdayEntry.entryDate, yesterdayEntry.entryContent))

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(listOf(yesterdayEntry))

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with an entry for today and an older entry WHEN getTimelineItems is called THEN return the list plus a placeholder for yesterday`() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "hello!")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "hiya!")
        val expectedList = listOf(TimelineEntry(todayEntry.entryDate, todayEntry.entryContent), TimelineEntry(
            LocalDate.now().minusDays(1), ""), TimelineEntry(oldEntry.entryDate, oldEntry.entryContent))

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(listOf(todayEntry, oldEntry))

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with an entry for yesterday and an older entry WHEN getTimelineItems is called THEN return the list plus a placeholder for today`() = runBlockingTest {
        val yesterdayEntry = Entry(LocalDate.now().minusDays(1), "hello!")
        val oldEntry = Entry(LocalDate.of(2011, 11, 11), "hiya!")
        val expectedList = listOf(TimelineEntry(LocalDate.now(), ""), TimelineEntry(
            yesterdayEntry.entryDate, yesterdayEntry.entryContent), TimelineEntry(oldEntry.entryDate, oldEntry.entryContent))

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(listOf(yesterdayEntry, oldEntry))

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with five entries WHEN getTimelineItems is called THEN return the list and a milestone`() = runBlockingTest {
        val todayEntry = Entry(LocalDate.now(), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(LocalDate.now().minusDays(i), "content"))
        }
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with five entries written three days in the past WHEN getTimelineItems is called THEN return the list with milestones and hints`() = runBlockingTest {
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

        expectedList.add(TimelineEntry(todayEntry.entryDate, todayEntry.entryContent))
        expectedList.add(TimelineEntry(yesterdayEntry.entryDate, yesterdayEntry.entryContent))
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with five entries written yesterday WHEN getTimelineItems is called THEN return the list with milestone and a hint for today`() = runBlockingTest {
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()

        val pastDays = mutableListOf<Entry>()
        for (i in 0L until 5L) {
            pastDays.add(Entry(LocalDate.now().minusDays(1 + i), "content"))
        }
        writtenDates.addAll(pastDays)

        expectedList.add(TimelineEntry(LocalDate.now(), ""))
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with five entries written starting today but skipping yesterday WHEN getTimelineItems is called THEN return the list with milestone and a hint for yesterday`() = runBlockingTest {
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

        expectedList.add(Milestone.create(5))
        expectedList.add(TimelineEntry(todayEntry.entryDate, todayEntry.entryContent))
        expectedList.add(TimelineEntry(LocalDate.now().minusDays(1), ""))
        expectedList.addAll(pastDays.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with five entries written starting yesterday WHEN getTimelineItems is called THEN return the list with milestone and a hint for today`() = runBlockingTest {
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

        expectedList.add(TimelineEntry(LocalDate.now(), ""))
        expectedList.add(Milestone.create(5))
        expectedList.add(TimelineEntry(yesterdayEntry.entryDate, yesterdayEntry.entryContent))
        expectedList.addAll(pastDays.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a user with many entries written before yesterday WHEN getTimelineItems is called THEN return the list with multiple milestones and placeholders`() = runBlockingTest {
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

        expectedList.add(TimelineEntry(LocalDate.now(), ""))
        expectedList.add(TimelineEntry(LocalDate.now().minusDays(1), ""))
        expectedList.add(Milestone.create(10))
        expectedList.addAll(pastDays.map { TimelineEntry(it.entryDate, it.entryContent) })
        expectedList.add(Milestone.create(5))
        expectedList.addAll(morePastDays.map { TimelineEntry(it.entryDate, it.entryContent) })

        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = flowOf(writtenDates)

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)

        val actual = viewModel.getTimelineItems().first()
        assertThat(actual).isEqualTo(expectedList)
    }

    @Test
    fun init_callsGetWrittenDates() = runBlockingTest {
        val expected = listOf(LocalDate.now())
        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = expected

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) = Unit

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)
        val actual = viewModel.getDatesWritten()

        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `GIVEN a list of entries WHEN addEntries is called THEN the local source is called`() {
        var addEntriesWasCalled = false
        val localSource = object : PresentlyLocalSource {
            override suspend fun getEntry(date: LocalDate): Entry { return Entry(date, "hii there") }

            override fun getEntriesFlow(): Flow<List<Entry>> = emptyFlow()

            override suspend fun getEntries(): List<Entry> = emptyList()

            override suspend fun getWrittenDates(): List<LocalDate> = emptyList()

            override suspend fun addEntry(entry: Entry) = Unit

            override suspend fun addEntries(entries: List<Entry>) {
                addEntriesWasCalled = true
            }

            override fun searchEntries(query: String): Flow<PagingData<Entry>> = flowOf(PagingData.empty())
        }

        val viewModel = TimelineViewModel(localSource)
        viewModel.addEntries(emptyList())

        assertThat(addEntriesWasCalled).isTrue()
    }
}
