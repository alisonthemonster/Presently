package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.CLICKED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.CLICKED_NEW_ENTRY
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH
import journal.gratitude.com.gratitudejournal.model.CLICKED_SETTINGS
import journal.gratitude.com.gratitudejournal.model.CLICKED_THEMES
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.OPENED_CONTACT_FORM
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val settings: PresentlySettings,
    private val analyticsLogger: AnalyticsLogger
) : ViewModel() {
    private val _state = MutableStateFlow(TimelineViewState())
    val state: StateFlow<TimelineViewState> = _state

    init {
        viewModelScope.launch {
            repository.getEntriesFlow().collect { list ->
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
                val yesterday = today.minus(1, DateTimeUnit.DAY)
                when {
                    list.isEmpty() -> {
                        _state.value = _state.value.copy(
                            timelineItems = persistentListOf<TimelineItem>(
                                Entry(today, ""),
                                Entry(yesterday, "")
                            )
                        )
                    }

                    list.size < 2 -> {
                        // user has only ever written one day
                        val newList = mutableListOf<TimelineItem>()
                        newList.addAll(list)
                        if (list[0].entryDate != today) {
                            newList.add(0, Entry(today, ""))
                        }
                        if (list[0].entryDate != yesterday) {
                            newList.add(1, Entry(yesterday, ""))
                        }
                        _state.value = _state.value.copy(
                            timelineItems = newList.toImmutableList(),
                            datesWritten = list.map {
                                it.entryDate
                            }.toSet()
                        )
                    }

                    else -> {
                        val latest = list[0]
                        val listWithHints = mutableListOf<Entry>()
                        listWithHints.addAll(list)
                        if (latest.entryDate != today) {
                            // they dont have the latest
                            listWithHints.add(0, Entry(today, ""))
                        }
                        if (listWithHints[1].entryDate != yesterday) {
                            listWithHints.add(1, Entry(yesterday, ""))
                        }

                        val listWithHintsAndMilestones = mutableListOf<TimelineItem>()
                        var numEntries = 0
                        for (index in listWithHints.size - 1 downTo 0) {
                            listWithHintsAndMilestones.add(0, listWithHints[index])
                            if (listWithHints[index].entryContent.isNotEmpty()) {
                                numEntries++
                                if (isMilestone(numEntries)) {
                                    listWithHintsAndMilestones.add(0, Milestone.create(numEntries))
                                }
                            }
                        }
                        _state.value = _state.value.copy(
                            timelineItems = listWithHintsAndMilestones.toImmutableList(),
                            datesWritten = list.map {
                                it.entryDate
                            }.toSet()
                        )
                    }
                }
            }
        }
    }

    fun loadSettings() {
        _state.value = _state.value.copy(
            shouldShowDayOfWeek = settings.shouldShowDayOfWeekInTimeline(),
            numberOfLinesPerRow = settings.getLinesPerEntryInTimeline()
        )
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun onEntryClicked(isNewEntry: Boolean) {
        if (isNewEntry) {
            analyticsLogger.recordEvent(CLICKED_NEW_ENTRY)
        } else {
            analyticsLogger.recordEvent(CLICKED_EXISTING_ENTRY)
        }
    }

    fun onSearchClicked() {
        analyticsLogger.recordEvent(CLICKED_SEARCH)
    }

    fun onThemesClicked() {
        analyticsLogger.recordEvent(CLICKED_THEMES)
    }

    fun onSettingsClicked() {
        analyticsLogger.recordEvent(CLICKED_SETTINGS)
    }

    fun onContactClicked() {
        analyticsLogger.recordEvent(OPENED_CONTACT_FORM)
    }

    fun logScreenView() {
        analyticsLogger.recordView("Timeline")
    }

    fun onFabClicked() {
        _state.value = _state.value.copy(
            isCalendarOpen = true
        )
    }

    fun dismissFab() {
        _state.value = _state.value.copy(
            isCalendarOpen = false
        )
    }
}
