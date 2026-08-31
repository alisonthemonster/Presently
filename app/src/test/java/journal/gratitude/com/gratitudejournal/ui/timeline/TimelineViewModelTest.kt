package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_VIEWED
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ShouldShowReminderOnboardingUseCase
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.LocalDate
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import java.util.Calendar
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TestTimelineRepository
    private lateinit var settings: PresentlySettings
    private lateinit var analytics: AnalyticsLogger
    private var showDayOfWeek = false
    private var linesPerEntry = 10
    private var firstDayOfWeek = Calendar.MONDAY

    @Before
    fun setUp() {
        repository = TestTimelineRepository()
        settings = mock()
        analytics = mock()
        whenever(settings.shouldShowDayOfWeekInTimeline()).thenAnswer { showDayOfWeek }
        whenever(settings.getLinesPerEntryInTimeline()).thenAnswer { linesPerEntry }
        whenever(settings.getFirstDayOfWeek()).thenAnswer { firstDayOfWeek }
        whenever(settings.hasSeenReminderOnboarding()).thenReturn(false)
    }

    @Test
    fun init_loadsTimelineRowsAndWrittenDates() = runTest {
        val today = LocalDate.now()
        repository.entriesFlow.emit(
            listOf(
                Entry(today, "Today"),
                Entry(today.minusDays(1), "Yesterday")
            )
        )
        repository.writtenDates.value = listOf(today, today.minusDays(1))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertThat(viewModel.state.value.items.filterIsInstance<TimelineEntryRowState>()).hasSize(2)
        assertThat(viewModel.state.value.writtenDates).containsExactly(today, today.minusDays(1))
    }

    @Test
    fun onTimelineEntryClicked_emitsEntryEffectAndRecordsAnalytics() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        val entry = TimelineEntryRowState(
            date = LocalDate.of(2026, 3, 28),
            dateText = "March 28, 2026",
            content = "",
            emptyHint = null,
            isCurrentDate = false,
            isNewEntry = true,
            numberExistingEntries = 4,
            maxLines = 10,
            isLastItem = false
        )

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onTimelineEntryClicked(entry)

        assertThat(effect.await()).isEqualTo(
            TimelineEffect.OpenEntry(
                clickedDate = entry.date,
                isNewEntry = true,
                numberExistingEntries = 4
            )
        )
        verify(analytics).recordEvent("clickedNewEntry")
    }

    @Test
    fun onCalendarClicked_updatesState() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onCalendarClicked()

        assertThat(viewModel.state.value.isCalendarVisible).isTrue()
        verify(analytics).recordEvent("clickedCalendar")
    }

    @Test
    fun onBackPressed_whenCalendarVisible_hidesCalendar() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()
        viewModel.onCalendarClicked()

        viewModel.onBackPressed()

        assertThat(viewModel.state.value.isCalendarVisible).isFalse()
    }

    @Test
    fun onReminderOnboardingResult_emitsPromptEffect() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onReminderOnboardingResult(savedBrandNewFirstEntry = true)

        assertThat(viewModel.state.value.showReminderOnboardingPrompt).isTrue()
        verify(analytics).recordEvent(REMINDER_ONBOARDING_PROMPT_VIEWED)
    }

    @Test
    fun onScreenResumed_refreshesRowsFromLatestSettings() = runTest {
        val entryDate = LocalDate.of(2026, 4, 10)
        repository.entriesFlow.emit(listOf(Entry(entryDate, "Entry content")))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val initialEntry = viewModel.state.value.items
            .filterIsInstance<TimelineEntryRowState>()
            .first { it.date == entryDate }
        assertThat(initialEntry.dateText).isEqualTo(entryDate.toFullString())
        assertThat(initialEntry.maxLines).isEqualTo(10)
        assertThat(viewModel.state.value.firstDayOfWeek).isEqualTo(DayOfWeek.MONDAY)

        showDayOfWeek = true
        linesPerEntry = 2
        firstDayOfWeek = Calendar.SUNDAY

        viewModel.onScreenResumed()
        advanceUntilIdle()

        val refreshedEntry = viewModel.state.value.items
            .filterIsInstance<TimelineEntryRowState>()
            .first { it.date == entryDate }
        assertThat(refreshedEntry.dateText).isEqualTo(entryDate.toStringWithDayOfWeek())
        assertThat(refreshedEntry.maxLines).isEqualTo(2)
        assertThat(viewModel.state.value.firstDayOfWeek).isEqualTo(DayOfWeek.SUNDAY)
    }

    private fun createViewModel(): TimelineViewModel {
        return TimelineViewModel(
            repository = repository,
            settings = settings,
            analytics = analytics,
            shouldShowReminderOnboarding = ShouldShowReminderOnboardingUseCase()
        )
    }
}

private class TestTimelineRepository : EntryRepository {
    val entriesFlow = MutableSharedFlow<List<Entry>>(replay = 1)
    val writtenDates = MutableLiveData<List<LocalDate>>(emptyList())

    override suspend fun getEntry(date: LocalDate): Entry? = null

    override suspend fun getRandomEntry(): Entry? = null

    override suspend fun getEntriesFlow(): Flow<List<Entry>> = entriesFlow

    override suspend fun getEntries(): List<Entry> = emptyList()

    override fun getWrittenDates(): LiveData<List<LocalDate>> = writtenDates

    override suspend fun addEntry(entry: Entry) = Unit

    override suspend fun addEntries(entries: List<Entry>) = Unit

    override fun searchEntries(query: String): Flow<PagingData<Entry>> = emptyFlow()
}
