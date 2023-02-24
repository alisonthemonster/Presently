package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineeViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val settings: PresentlySettings
) : ViewModel() {
    private val _state = MutableStateFlow(TimelineViewState())
    val state: StateFlow<TimelineViewState> = _state

    init {
        viewModelScope.launch {
            repository.getEntriesFlow().collect { list ->
                val today = LocalDate.now()
                val yesterday = LocalDate.now().minusDays(1)
                when {
                    list.isEmpty() -> {
                        _state.value = _state.value.copy(
                            timelineItems = listOf<TimelineItem>(
                                Entry(today, ""),
                                Entry(yesterday, "")
                            )
                        )
                    }
                    list.size < 2 -> {
                        //user has only ever written one day
                        val newList = mutableListOf<TimelineItem>()
                        newList.addAll(list)
                        if (list[0].entryDate != today) {
                            newList.add(0, Entry(today, ""))
                        }
                        if (list[0].entryDate != yesterday) {
                            newList.add(1, Entry(yesterday, ""))
                        }
                        _state.value = _state.value.copy(
                            timelineItems = newList,
                            datesWritten = list.map {
                                it.entryDate
                            }.toSet())
                    }
                    else -> {
                        val latest = list[0]
                        val listWithHints = mutableListOf<Entry>()
                        listWithHints.addAll(list)
                        if (latest.entryDate != today) {
                            //they dont have the latest
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
                            timelineItems = listWithHintsAndMilestones,
                            datesWritten = list.map {
                                it.entryDate
                            }.toSet()
                        )
                    }
                }
            }
        }

//        viewModelScope.launch {
//            settings.getAuthenticationState().collect { authState ->
//                _state.value = _state.value.copy(authenticationState = authState)
//            }
//        }
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }
}
