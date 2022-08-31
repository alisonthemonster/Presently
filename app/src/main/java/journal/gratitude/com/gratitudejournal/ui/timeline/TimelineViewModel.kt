package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.coroutine_utils.AppCoroutineDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    fun getTimelineItems(): Flow<List<TimelineItem>> {
        return repository.getEntriesFlow().map { list ->
            val today = LocalDate.now()
            val yesterday = LocalDate.now().minusDays(1)
            when {
                list.isEmpty() -> {
                    listOf<TimelineItem>(
                        Entry(today, ""),
                        Entry(yesterday, "")
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
                    newList
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
                    listWithHintsAndMilestones
                }
            }
        }
    }

    suspend fun getDatesWritten(): List<LocalDate> {
        return repository.getWrittenDates()
    }

    fun addEntries(entries: List<Entry>) = viewModelScope.launch(dispatchers.io) {
        repository.addEntries(entries)
    }
}
