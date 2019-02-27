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
            val yesterday = LocalDate.now().minusDays(1)
            if (list.isEmpty()) {
                listOf(
                    Entry(today, ""),
                    Entry(yesterday, "")
                )
            } else if (list.size < 2) {
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
            } else {
                val latest = list[0]
                val previous = list[1]
                val newList = mutableListOf<Entry>()
                newList.addAll(list)
                if (latest.entryDate != today) {
                    //they dont have the latest
                    newList.add(0, Entry(today, ""))

                } else if (previous.entryDate != yesterday && latest.entryDate != yesterday) {
                    //they dont have yesterday
                    newList.add(1, Entry(yesterday, ""))

                }
                newList
            }
        }
    }

    fun getEntriesList(): List<Entry> {
        return entries.value ?: emptyList()
    }
}
