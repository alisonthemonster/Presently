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
import journal.gratitude.com.gratitudejournal.logging.BACKUP_CARD_ACTION_CLICKED
import journal.gratitude.com.gratitudejournal.logging.BACKUP_CARD_DISMISSED
import journal.gratitude.com.gratitudejournal.logging.BACKUP_CARD_STATUS_KEY
import journal.gratitude.com.gratitudejournal.logging.BACKUP_CARD_VIEWED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PROMPT_VIEWED
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ShouldShowReminderOnboardingUseCase
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.BackupStatusStore
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatus
import journal.gratitude.com.gratitudejournal.util.backups.status.BackupStatusResolver
import journal.gratitude.com.gratitudejournal.util.wiring.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.time.Clock
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val settings: PresentlySettings,
    private val backupStatusStore: BackupStatusStore,
    private val analytics: AnalyticsLogger,
    private val shouldShowReminderOnboarding: ShouldShowReminderOnboardingUseCase,
    private val clock: Clock,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val writtenDatesLiveData = repository.getWrittenDates()
    private var latestEntries = emptyList<Entry>()
    private var latestRows = emptyList<TimelineRowState>()
    private var backupCard: TimelineCardRowState? = null
    private var backupJob: Job? = null
    private var backupStatus: BackupStatus = BackupStatus.Healthy
    private var lastLoggedBackupCardStatus: String? = null

    private val _state = MutableStateFlow(TimelineUiState())
    val state = _state.asStateFlow()

    private val _effects = MutableSharedFlow<TimelineEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects = _effects.asSharedFlow()

    init {
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

    fun onTimelineCardActionClicked(action: TimelineCardAction) {
        analytics.recordEvent(BACKUP_CARD_ACTION_CLICKED, backupCardDetails())
        emitEffect(
            when (action) {
                TimelineCardAction.ReconnectDropbox -> TimelineEffect.ReconnectDropbox
                TimelineCardAction.ExportBackup -> TimelineEffect.ExportBackup
                TimelineCardAction.OpenSettings -> TimelineEffect.OpenBackupSettings
            }
        )
    }

    fun onTimelineCardDismissed(card: TimelineCardRowState) {
        if (card.id == BACKUP_CARD_ID && card.isDismissible) {
            analytics.recordEvent(BACKUP_CARD_DISMISSED, backupCardDetails())
            backupStatusStore.recordBannerDismissal(backupStatus.type)
            backupCard = null
            publishItems()
            refreshBackupCard()
        }
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
        latestRows = latestEntries.toTimelineRowStates(
            showDayOfWeek = settings.shouldShowDayOfWeekInTimeline(),
            linesPerEntry = settings.getLinesPerEntryInTimeline(),
            fontScale = settings.getTimelineFontScale() / 100f
        )
        publishItems()
        refreshBackupCard()
    }

    private fun publishItems() {
        _state.value = _state.value.copy(
            firstDayOfWeek = settings.getFirstDayOfWeek().toDayOfWeek(),
            items = listOfNotNull(backupCard) + latestRows
        )
    }

    /**
     * Resolves the backup status off the main thread (it reads preferences, parses the Dropbox
     * credential and walks every entry). A newer refresh cancels an older one still running.
     */
    private fun refreshBackupCard() {
        backupJob?.cancel()
        val entries = latestEntries
        backupJob = viewModelScope.launch {
            val resolved = withContext(defaultDispatcher) {
                val now = clock.millis()
                val status = BackupStatusResolver.resolve(
                    buildBackupStatusInput(settings, backupStatusStore, entries, now, clock.zone)
                )
                val show = BackupStatusResolver.shouldShowBanner(
                    status,
                    backupStatusStore.getBannerDismissal(),
                    now
                )
                status to if (show) status.toTimelineCard(now) else null
            }
            backupStatus = resolved.first
            backupCard = resolved.second
            logBackupCardViewed(backupCard)
            publishItems()
        }
    }

    private fun backupCardDetails() =
        mapOf<String, Any>(BACKUP_CARD_STATUS_KEY to backupStatus.type.name.lowercase())

    /** Once per change of shown status, not on every refresh. */
    private fun logBackupCardViewed(card: TimelineCardRowState?) {
        val status = if (card != null) backupStatus.type.name.lowercase() else null
        if (status != null && status != lastLoggedBackupCardStatus) {
            analytics.recordEvent(BACKUP_CARD_VIEWED, backupCardDetails())
        }
        lastLoggedBackupCardStatus = status
    }

    private fun Int.toDayOfWeek(): DayOfWeek {
        return when (this) {
            Calendar.SATURDAY -> DayOfWeek.SATURDAY
            Calendar.SUNDAY -> DayOfWeek.SUNDAY
            else -> DayOfWeek.MONDAY
        }
    }
}
