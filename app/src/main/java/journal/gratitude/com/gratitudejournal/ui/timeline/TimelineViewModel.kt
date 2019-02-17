package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.threeten.bp.LocalDate

class TimelineViewModel(repository: EntryRepository) : ViewModel() {

    val entries: LiveData<List<Entry>>

    init {
        entries = Transformations.map(repository.getAllEntries()) { list ->
            val today = LocalDate.now()
            if (list.isEmpty()) {
                val newList = mutableListOf<Entry>()
                newList.add(Entry(today, ""))
                newList
            } else if (list[0].entryDate != today) {
                val newList = mutableListOf<Entry>()
                newList.add(Entry(today, ""))
                newList.addAll(list)
                newList
            } else {
                list
            }
        }
    }

    fun getEntriesList(): List<Entry> {
        return entries.value ?: emptyList()
    }
}
