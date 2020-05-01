package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SettingsViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    suspend fun getEntries(): List<Entry> = withContext(Dispatchers.IO) {
        repository.getEntries()
    }

    fun addEntries(entries: List<Entry>) = scope.launch(Dispatchers.IO) {
        repository.addEntries(entries)
    }

}
