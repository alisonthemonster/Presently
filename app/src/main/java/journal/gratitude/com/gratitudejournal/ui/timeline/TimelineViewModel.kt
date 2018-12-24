package journal.gratitude.com.gratitudejournal.ui.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

class TimelineViewModel(repository: EntryRepository) : ViewModel() {

    val entries: LiveData<List<Entry>> = repository.getAllEntries()

    fun getEntriesList(): List<Entry> {
        return entries.value ?: emptyList()
    }
}
