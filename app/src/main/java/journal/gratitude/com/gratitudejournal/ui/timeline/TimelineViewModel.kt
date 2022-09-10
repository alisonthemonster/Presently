package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.isMilestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.appendTodayAndYesterday
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

@HiltViewModel
class TimelineViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    val entries = MutableLiveData<List<TimelineItem>>()
    val datesWritten: LiveData<List<LocalDate>> = repository.getWrittenDates()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        scope.launch {
            repository.getEntriesFlow().collect { list ->
                val listWithAppendedTodayAndYesterday = appendTodayAndYesterday(list)
                val listWithHintsAndMilestones = mutableListOf<TimelineItem>()
                var numEntries = 0
                for (index in listWithAppendedTodayAndYesterday.size - 1 downTo 0) {
                    listWithHintsAndMilestones.add(0, listWithAppendedTodayAndYesterday[index])
                    if (listWithAppendedTodayAndYesterday[index].entryContent.isNotEmpty()) {
                        numEntries++
                        if (isMilestone(numEntries)) {
                            listWithHintsAndMilestones.add(0, Milestone.create(numEntries))
                        }
                    }
                }
                entries.value = listWithHintsAndMilestones
            }
        }
    }

    fun getTimelineItems(): List<TimelineItem> {
        return entries.value ?: emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
