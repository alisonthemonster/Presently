package journal.gratitude.com.gratitudejournal.ui.timeline

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository

class TimelineViewModel(private val repository: EntryRepository) : ViewModel() {

    val allEntries: LiveData<List<Entry>> = repository.getAllEntries()
}
