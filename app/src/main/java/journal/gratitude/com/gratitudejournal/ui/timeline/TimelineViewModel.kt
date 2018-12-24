package journal.gratitude.com.gratitudejournal.ui.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import org.threeten.bp.LocalDate

class TimelineViewModel(private val repository: EntryRepository) : ViewModel() {

    val entries: LiveData<List<Entry>>

    init {
        entries = Transformations.map(repository.getAllEntries()) { list ->
            val today = LocalDate.now()
            if (list.isEmpty()) {
                emptyList()
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
