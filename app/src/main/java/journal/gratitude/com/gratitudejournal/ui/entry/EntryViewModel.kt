package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import kotlin.coroutines.CoroutineContext

class EntryViewModel(dateString: String, private val repository: EntryRepository) : ViewModel() {

    val entry: LiveData<Entry>
    val entryContent = ObservableField<String>("")

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val date: LocalDate = dateString.toLocalDate()

    init {
        entry = Transformations.map(repository.getEntry(date)) { entry ->
            if (entry != null) {
                entryContent.set(entry.entryContent)
            }
            entry
        }
    }

    fun addNewEntry() = scope.launch(Dispatchers.IO) {
        val entry = Entry(date, entryContent.get() ?: "")
        repository.addEntry(entry)
    }

    fun getDateString(): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Today" //TODO move to resources
            today.minusDays(1) -> "Yesterday"
            else -> date.toFullString()
        }
    }

    fun getThankfulString(): String {
        return if (date == LocalDate.now()) {
            "I am thankful for"
        } else {
            "I was thankful for"
        }
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}
