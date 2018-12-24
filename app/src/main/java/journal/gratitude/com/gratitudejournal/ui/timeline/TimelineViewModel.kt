package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class TimelineViewModel(application: Application,  private val repository: EntryRepository) : AndroidViewModel(application) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    val wordsDao = EntryDatabase.getDatabase(application).entryDao()
    val allEntries: LiveData<List<Entry>> = repository.getAllEntries()

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
