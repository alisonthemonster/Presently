package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.model.Entry
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.milestones
import journal.gratitude.com.gratitudejournal.model.TimelineEntry
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@HiltViewModel
class TimelineViewModel @Inject constructor(private val localSource: PresentlyLocalSource) : ViewModel() {

    suspend fun getDatesWritten(): List<LocalDate> = localSource.getWrittenDates()

    fun getTimelineItems(): Flow<List<TimelineItem>> {
        return localSource.getEntriesFlow().map { entries ->
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)

            when {
                entries.isEmpty() -> {
                    //create placeholders for today and yesterday
                    listOf<TimelineItem>(
                        TimelineEntry(today, ""),
                        TimelineEntry(yesterday, "")
                    )
                }
                entries.size < 2 -> {
                    //user has only ever written one day
                    val newList = mutableListOf<TimelineItem>()
                    newList.addAll(entries.map { TimelineEntry(it.entryDate, it.entryContent) })
                    if (entries[0].entryDate != today) {
                        newList.add(0, TimelineEntry(today, ""))
                    }
                    if (entries[0].entryDate != yesterday) {
                        newList.add(1, TimelineEntry(yesterday, ""))
                    }
                    newList
                }
                entries.size < 2 -> {
                    //user has only ever written one day
                    val newList: MutableList<TimelineItem> =
                        entries.map { TimelineEntry(it.entryDate, it.entryContent) }.toMutableList()
                    if (entries[0].entryDate != today) {
                        newList.add(0, TimelineEntry(today, ""))
                    }
                    if (entries[0].entryDate != yesterday) {
                        newList.add(1, TimelineEntry(yesterday, ""))
                    }
                    newList
                }
                else -> {
                    val listWithPlaceholders  =
                        entries.map { TimelineEntry(it.entryDate, it.entryContent) }.toMutableList()
                    val latest = entries[0]
                    if (latest.entryDate != today) {
                        listWithPlaceholders.add(0, TimelineEntry(today, ""))
                    }
                    if (listWithPlaceholders[1].date != yesterday) {
                        listWithPlaceholders.add(1, TimelineEntry(yesterday, ""))
                    }

                    val listWithHintsAndMilestones = mutableListOf<TimelineItem>()
                    var numEntries = 0
                    for (index in listWithPlaceholders.size - 1 downTo 0) {
                        listWithHintsAndMilestones.add(0, listWithPlaceholders[index])
                        if (listWithPlaceholders[index].content.isNotEmpty()) {
                            numEntries++
                            if (milestones.contains(numEntries)) {
                                listWithHintsAndMilestones.add(0, Milestone.create(numEntries))
                            }
                        }
                    }
                    listWithHintsAndMilestones
                }
            }
        }
    }

    fun addEntries(entries: List<Entry>) {
        viewModelScope.launch {
            localSource.addEntries(entries)
        }
    }
}
