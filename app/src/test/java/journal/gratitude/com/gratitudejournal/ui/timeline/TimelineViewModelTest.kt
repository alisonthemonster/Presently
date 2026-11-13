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
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: TestTimelineRepository
    private lateinit var settings: PresentlySettings
    private lateinit var analytics: AnalyticsLogger
    private lateinit var backupStatusStore: journal.gratitude.com.gratitudejournal.settings.BackupStatusStore
    private val clock = java.time.Clock.fixed(java.time.Instant.parse("2026-11-13T12:00:00Z"), java.time.ZoneOffset.UTC)
    private var showDayOfWeek = false
    private var linesPerEntry = 10
    private var firstDayOfWeek = Calendar.MONDAY

    @Before
    fun setUp() {
        repository = TestTimelineRepository()
        settings = mock()
        analytics = mock()
        backupStatusStore = mock()
        // A Mockito mock returns 0 for a nullable Long, which would read as a backup in 1970.
        whenever(backupStatusStore.getLastDropboxSuccessAt()).thenReturn(null)
        whenever(backupStatusStore.getLastManualExportAt()).thenReturn(null)
        whenever(backupStatusStore.getDropboxEnabledAt()).thenReturn(null)
        whenever(settings.getAutomaticBackupCadence()).thenReturn(journal.gratitude.com.gratitudejournal.settings.BackupCadence.DAILY)
        whenever(settings.shouldShowDayOfWeekInTimeline()).thenAnswer { showDayOfWeek }
        whenever(settings.getLinesPerEntryInTimeline()).thenAnswer { linesPerEntry }
        whenever(settings.getTimelineFontScale()).thenReturn(100)
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

    @Test
    fun neverBackedUp_showsCardFirst_andDismissRecordsAndHidesIt() = runTest {
        val today = LocalDate.now()
        repository.entriesFlow.emit((0 until 12).map { Entry(today.minusDays(it.toLong()), "x") })
        var dismissal: journal.gratitude.com.gratitudejournal.util.backups.status.BackupBannerDismissal? = null
        whenever(backupStatusStore.getBannerDismissal()).thenAnswer { dismissal }
        whenever(backupStatusStore.recordBannerDismissal(com.nhaarman.mockitokotlin2.any())).thenAnswer {
            dismissal = journal.gratitude.com.gratitudejournal.util.backups.status.BackupBannerDismissal(
                journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusType.NEVER_BACKED_UP,
                clock.millis()
            )
            Unit
        }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val card = viewModel.state.value.items.first() as TimelineCardRowState
        assertThat(card.isDismissible).isTrue()

        verify(analytics).recordEvent("backupCardViewed", mapOf("status" to "never_backed_up"))

        viewModel.onTimelineCardActionClicked(card.action)
        verify(analytics).recordEvent("backupCardActionClicked", mapOf("status" to "never_backed_up"))

        viewModel.onTimelineCardDismissed(card)
        verify(analytics).recordEvent("backupCardDismissed", mapOf("status" to "never_backed_up"))

        assertThat(viewModel.state.value.items.filterIsInstance<TimelineCardRowState>()).isEmpty()
    }

    @Test
    fun healthy_showsNoCard() = runTest {
        repository.entriesFlow.emit(listOf(Entry(LocalDate.now(), "x")))
        val viewModel = createViewModel()
        advanceUntilIdle()
        assertThat(viewModel.state.value.items.filterIsInstance<TimelineCardRowState>()).isEmpty()
    }

    private fun createViewModel(): TimelineViewModel {
        return TimelineViewModel(
            repository = repository,
            settings = settings,
            backupStatusStore = backupStatusStore,
            analytics = analytics,
            shouldShowReminderOnboarding = ShouldShowReminderOnboardingUseCase(),
            clock = clock,
            defaultDispatcher = mainDispatcherRule.dispatcher
        )
    }
}

private class TestTimelineRepository : EntryRepository {
    val entriesFlow = MutableSharedFlow<List<Entry>>(replay = 1)
    val writtenDates = MutableLiveData<List<LocalDate>>(emptyList())

    override suspend fun getEntry(date: LocalDate): Entry? = null

    override suspend fun getEntriesFlow(): Flow<List<Entry>> = entriesFlow

    override suspend fun getEntries(): List<Entry> = emptyList()

    override fun getWrittenDates(): LiveData<List<LocalDate>> = writtenDates

    override suspend fun addEntry(entry: Entry) = Unit

    override suspend fun addEntries(entries: List<Entry>) = Unit

    override suspend fun deleteAllEntries() = Unit

    override fun searchEntries(query: String): Flow<PagingData<Entry>> = emptyFlow()
}
