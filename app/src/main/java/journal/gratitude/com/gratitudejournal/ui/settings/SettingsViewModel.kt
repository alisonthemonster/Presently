package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val repository: EntryRepository) : ViewModel() {

    suspend fun getEntries(): List<Entry> = withContext(Dispatchers.IO) {
        repository.getEntries()
    }

    fun addEntries(entries: List<Entry>) = viewModelScope.launch(Dispatchers.IO) {
        repository.addEntries(entries)
    }

}
