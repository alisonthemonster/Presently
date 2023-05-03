package journal.gratitude.com.gratitudejournal.ui.timeline

import com.google.common.truth.Truth.assertThat
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.MainDispatcherRule
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TimelineViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = mock<EntryRepository>()
    private val settings = mock<PresentlySettings>()
    private val analytics = mock<AnalyticsLogger>()

    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    private val yesterday = today.minus(1, DateTimeUnit.DAY)

    @Test
    fun init_emptylistWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntries() = runTest {
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(emptyList<Entry>()))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(listOf(todayEntry, yesterdayEntry))
    }

    @Test
    fun init_listWithoutTodayOrYesterdayWritten_addsEmptyTodayAndYesterdayEntriesToList() = runTest {
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")
        val oldEntry = Entry(LocalDate(2011, 11, 11), "")
        val oldEntry1 = Entry(LocalDate(2011, 11, 10), "")
        val oldEntry2 = Entry(LocalDate(2011, 11, 9), "")

        val list = listOf(oldEntry, oldEntry1, oldEntry2)
        whenever(repository.getEntriesFlow()).thenReturn(flowOf(list))

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry, oldEntry1, oldEntry2)

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithTodayAndYesterdayWritten_returnsOriginalList() = runTest {
        val todayEntry = Entry(today, "hello!")
        val yesterdayEntry = Entry(yesterday, "howdy")
        val expectedList = listOf(todayEntry, yesterdayEntry)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(expectedList))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsOriginalListPlusYesterday() = runTest {
        val todayEntry = Entry(today, "hello!")
        val yesterdayEntry = Entry(yesterday, "")
        val oldEntry = Entry(LocalDate(2011, 11, 11), "")

        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)
        whenever(repository.getEntriesFlow()).thenReturn(flowOf(expectedList))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithTodayWrittenNoYesterday_returnsTodayPlusYesterday() = runTest {
        val todayEntry = Entry(today, "hello!")
        val yesterdayEntry = Entry(yesterday, "")
        val expectedList = listOf(todayEntry, yesterdayEntry)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(expectedList))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsOriginalListPlusToday() = runTest {
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")
        val oldEntry = Entry(LocalDate(2011, 11, 11), "")
        val expectedList = listOf(todayEntry, yesterdayEntry, oldEntry)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(expectedList))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithYesterdayWrittenNoToday_returnsYesterdayPlusToday() = runTest {
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")
        val expectedList = listOf(todayEntry, yesterdayEntry)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(expectedList))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithFiveEntries_returnsFiveEntriesAndMilestone() = runTest {
        val todayEntry = Entry(today, "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(today.minus(i, DateTimeUnit.DAY), "content"))
        }
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithFiveEntriesWrittenThreeDaysAgo_returnsFiveEntriesAndMilestoneAndHints() = runTest {
        val firstEntry = Entry(today.minus(3, DateTimeUnit.DAY), "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(firstEntry)
        for (i in 1L until 5L) {
            writtenDates.add(Entry(today.minus(3 + i, DateTimeUnit.DAY), "content"))
        }

        // empty entries for user to fill in today and yesterday
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")

        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(writtenDates)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithFiveEntriesWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHint() = runTest {
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()

        val pastDays = mutableListOf<Entry>()
        for (i in 0L until 5L) {
            pastDays.add(Entry(today.minus(1 + i, DateTimeUnit.DAY), "content"))
        }
        writtenDates.addAll(pastDays)

        // empty entry for user to fill in today
        val todayEntry = Entry(today, "")

        expectedList.add(todayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(pastDays)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithFiveEntriesWrittenWithGapYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() = runTest {
        // the milestone entry is written today and yesterday was not filled in
        // the milestone should appear before today's entry which is followed by a hint

        val todayEntry = Entry(today, "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(todayEntry)
        val pastDays = mutableListOf<Entry>()
        for (i in 1L until 5L) {
            pastDays.add(Entry(today.minus(5 + i, DateTimeUnit.DAY), "content"))
        }
        writtenDates.addAll(pastDays)

        // empty entry for user to fill in yesterday
        val yesterdayEntry = Entry(yesterday, "")

        expectedList.add(Milestone.create(5))
        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.addAll(pastDays)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithMilestoneWrittenYesterday_returnsFiveEntriesAndMilestoneAndOneHintInCorrectOrder() = runTest {
        // the milestone entry is written yesterday and today is not filled in
        // the milestone should appear after today's entry and then followed by a hint

        val yesterdayEntry = Entry(yesterday, "content")
        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        writtenDates.add(yesterdayEntry)
        val pastDays = mutableListOf<Entry>()
        for (i in 1L until 5L) {
            // write four entries in the past
            pastDays.add(Entry(today.minus(10 + i, DateTimeUnit.DAY), "content"))
        }
        writtenDates.addAll(pastDays)

        // empty entry for user to fill in today
        val todayEntry = Entry(today, "")

        expectedList.add(todayEntry)
        expectedList.add(Milestone.create(5))
        expectedList.add(yesterdayEntry)
        expectedList.addAll(pastDays)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun init_listWithMultipleMilestones_returnsCorrectOrderList() = runTest {
        // multiple milestones written in the past

        val writtenDates = mutableListOf<Entry>()
        val expectedList = mutableListOf<TimelineItem>()
        val pastDays = mutableListOf<Entry>()
        for (i in 5L until 10L) {
            // write five entries in the past
            pastDays.add(Entry(today.minus(i, DateTimeUnit.DAY), "content"))
        }
        val morePastDays = mutableListOf<Entry>()
        for (i in 10L until 15L) {
            // write five entries in the more distant past
            morePastDays.add(Entry(today.minus(10 + i, DateTimeUnit.DAY), "content"))
        }
        writtenDates.addAll(pastDays)
        writtenDates.addAll(morePastDays)

        // empty entries for user to fill in today and yesterday
        val todayEntry = Entry(today, "")
        val yesterdayEntry = Entry(yesterday, "")

        expectedList.add(todayEntry)
        expectedList.add(yesterdayEntry)
        expectedList.add(Milestone.create(10))
        expectedList.addAll(pastDays)
        expectedList.add(Milestone.create(5))
        expectedList.addAll(morePastDays)

        whenever(repository.getEntriesFlow()).thenReturn(flowOf(writtenDates))

        val viewModel = TimelineViewModel(repository, settings, analytics)

        assertThat(viewModel.state.value.timelineItems).isEqualTo(expectedList)
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN getSelectedTheme is called THEN fetch the theme`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)
        val expected = "MyTheme"
        whenever(settings.getCurrentTheme()).thenReturn(expected)

        viewModel.getSelectedTheme()

        verify(settings).getCurrentTheme()
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onEntryClicked is called with new entry THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onEntryClicked(true)

        verify(analytics).recordEvent("clickedNewEntry")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onEntryClicked is called with existing entry THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onEntryClicked(false)

        verify(analytics).recordEvent("clickedExistingEntry")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onThemesClicked THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onThemesClicked()

        verify(analytics).recordEvent("clickedThemes")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onSearchClicked THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onSearchClicked()

        verify(analytics).recordEvent("clickedSearch")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onSettingsClicked THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onSettingsClicked()

        verify(analytics).recordEvent("clickedSettings")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN onContactClicked THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.onContactClicked()

        verify(analytics).recordEvent("openedContactForm")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN logScreenView THEN log an analytics event`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)

        viewModel.logScreenView()

        verify(analytics).recordView("Timeline")
    }

    @Test
    fun `GIVEN a TimelineViewModel WHEN loadSettings is called THEN update the state`() {
        val viewModel = TimelineViewModel(repository, settings, analytics)
        val shouldShowDayOfWeekInTimeline = true
        whenever(settings.shouldShowDayOfWeekInTimeline()).thenReturn(shouldShowDayOfWeekInTimeline)
        val linesPerEntryInTimeline = 34
        whenever(settings.getLinesPerEntryInTimeline()).thenReturn(linesPerEntryInTimeline)

        viewModel.loadSettings()

        verify(settings).shouldShowDayOfWeekInTimeline()
        verify(settings).getLinesPerEntryInTimeline()
        assertThat(viewModel.state.value.numberOfLinesPerRow).isEqualTo(linesPerEntryInTimeline)
        assertThat(viewModel.state.value.shouldShowDayOfWeek).isEqualTo(shouldShowDayOfWeekInTimeline)
    }
}
