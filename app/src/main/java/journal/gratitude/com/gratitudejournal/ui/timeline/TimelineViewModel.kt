package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class TimelineViewModel @Inject constructor(val repository: EntryRepository) : ViewModel() {

    val entries: LiveData<List<Entry>>
    val datesWritten: LiveData<List<LocalDate>> = repository.getWrittenDates()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        entries = Transformations.map(repository.getAllEntries()) { list ->
            val today = LocalDate.now()
            val yesterday = LocalDate.now().minusDays(1)
            when {
                list.isEmpty() -> listOf(
                    Entry(today, ""),
                    Entry(yesterday, "")
                )
                list.size < 2 -> {
                    //user has only ever written one day
                    val newList = mutableListOf<Entry>()
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
                    val newList = mutableListOf<Entry>()
                    newList.addAll(list)
                    if (latest.entryDate != today) {
                        //they dont have the latest
                        newList.add(0, Entry(today, ""))
                    }
                    if (newList[1].entryDate != yesterday) {
                        newList.add(1, Entry(yesterday, ""))
                    }
                    newList
                }
            }
        }
    }

    fun getEntriesList(): List<Entry> {
        return entries.value ?: emptyList()
    }

    fun addEntries(entries: List<Entry>) = scope.launch(Dispatchers.IO) {
        repository.addEntries(entries)
    }
}
