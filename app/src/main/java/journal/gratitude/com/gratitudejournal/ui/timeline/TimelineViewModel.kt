package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.CLICKED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.CLICKED_EXISTING_ENTRY_CALENDAR
import journal.gratitude.com.gratitudejournal.model.CLICKED_NEW_ENTRY
import journal.gratitude.com.gratitudejournal.model.CLICKED_NEW_ENTRY_CALENDAR
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.LOOKED_AT_SETTINGS
import journal.gratitude.com.gratitudejournal.model.OPENED_CALENDAR
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_VIEWED
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ShouldShowReminderOnboardingUseCase
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Calendar
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val settings: PresentlySettings,
    private val analytics: AnalyticsLogger,
    private val shouldShowReminderOnboarding: ShouldShowReminderOnboardingUseCase
) : ViewModel() {

    private val writtenDatesLiveData = repository.getWrittenDates()
    private var latestEntries = emptyList<Entry>()

    private val _state = MutableStateFlow(TimelineUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TimelineEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects = _effects.asSharedFlow()

    init {
        val firstDayOfWeek = when (settings.getFirstDayOfWeek()) {
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
        _state.value = _state.value.copy(firstDayOfWeek = firstDayOfWeek)

        viewModelScope.launch {
            repository.getEntriesFlow().collect { entries ->
                latestEntries = entries
                refreshTimelineRows()
            }
        }

        viewModelScope.launch {
            val writtenDatesFlow = writtenDatesLiveData
                .asFlow()
                .onStart { emit(writtenDatesLiveData.value ?: emptyList()) }
            writtenDatesFlow.collect { writtenDates ->
                _state.value = _state.value.copy(writtenDates = writtenDates)
            }
        }
    }

    fun onSearchClicked() {
        analytics.recordEvent(CLICKED_SEARCH)
        emitEffect(TimelineEffect.OpenSearch)
    }

    fun onSettingsClicked() {
        analytics.recordEvent(LOOKED_AT_SETTINGS)
        emitEffect(TimelineEffect.OpenSettings)
    }

    fun onTimelineEntryClicked(entry: TimelineEntryRowState) {
        analytics.recordEvent(
            if (entry.isNewEntry) CLICKED_NEW_ENTRY else CLICKED_EXISTING_ENTRY
        )
        emitEffect(
            TimelineEffect.OpenEntry(
                clickedDate = entry.date,
                isNewEntry = entry.isNewEntry,
                numberExistingEntries = entry.numberExistingEntries
            )
        )
    }

    fun onCalendarClicked() {
        analytics.recordEvent(OPENED_CALENDAR)
        _state.value = _state.value.copy(isCalendarVisible = true)
    }

    fun onCalendarClosed() {
        _state.value = _state.value.copy(isCalendarVisible = false)
    }

    fun onCalendarDateClicked(date: LocalDate, isNewEntry: Boolean, numberExistingEntries: Int) {
        analytics.recordEvent(
            if (isNewEntry) CLICKED_NEW_ENTRY_CALENDAR else CLICKED_EXISTING_ENTRY_CALENDAR
        )
        emitEffect(
            TimelineEffect.OpenEntry(
                clickedDate = date,
                isNewEntry = isNewEntry,
                numberExistingEntries = numberExistingEntries
            )
        )
    }

    fun onBackPressed() {
        if (_state.value.isCalendarVisible) {
            _state.value = _state.value.copy(isCalendarVisible = false)
        } else {
            emitEffect(TimelineEffect.ExitTimeline)
        }
    }

    fun onScreenResumed() {
        refreshTimelineRows()
    }

    fun onReminderOnboardingResult(savedBrandNewFirstEntry: Boolean) {
        if (
            shouldShowReminderOnboarding(
                savedBrandNewFirstEntry = savedBrandNewFirstEntry,
                hasSeenReminderOnboarding = settings.hasSeenReminderOnboarding()
            )
        ) {
            analytics.recordEvent(REMINDER_ONBOARDING_PROMPT_VIEWED)
            _state.value = _state.value.copy(showReminderOnboardingPrompt = true)
        }
    }

    fun onReminderOnboardingPromptHandled() {
        _state.value = _state.value.copy(showReminderOnboardingPrompt = false)
    }

    private fun emitEffect(effect: TimelineEffect) {
        _effects.tryEmit(effect)
    }

    private fun refreshTimelineRows() {
        _state.value = _state.value.copy(
            items = latestEntries.toTimelineRowStates(
                showDayOfWeek = settings.shouldShowDayOfWeekInTimeline(),
                linesPerEntry = settings.getLinesPerEntryInTimeline()
            )
        )
    }
}
