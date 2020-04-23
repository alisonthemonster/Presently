package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.lifecycle.ViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    suspend fun getEntries(): List<Entry> = withContext(Dispatchers.IO) {
        repository.getEntries()
    }

}
