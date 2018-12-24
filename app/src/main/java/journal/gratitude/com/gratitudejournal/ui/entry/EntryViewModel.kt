package journal.gratitude.com.gratitudejournal.ui.entry

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.SingleLiveEvent
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import kotlin.coroutines.CoroutineContext

class EntryViewModel(dateString: String, private val repository: EntryRepository) : ViewModel() {

    private val date = dateString.toLocalDate()

    var entry: LiveData<Entry>

    val entryContent = ObservableField<String>("")
    private val isSaved = ObservableBoolean()
    private val isSaving = ObservableBoolean()

    val errorLiveEvent = SingleLiveEvent<String>()

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        entry = Transformations.map(repository.getEntry(date)) {entry ->
            if (entry != null) {
                if (entry.entryContent.isEmpty()) {
                    isSaved.set(false)
                    isSaving.set(false)
                    entryContent.set("")
                } else {
                    isSaved.set(true)
                    isSaving.set(false)
                    entryContent.set(entry.entryContent)
                }
            }
            entry
        }
    }

    fun addNewEntry() = scope.launch(Dispatchers.IO) {
        val entry = Entry(date, entryContent.get() ?: "")

        repository.addEntry(entry)
        isSaved.set(true)
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

    fun getSavingString(): String {
        return when {
            isSaving.get() -> "Saving"
            isSaved.get() -> "Saved"
            else -> ""
        }
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }

}
