package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import journal.gratitude.com.gratitudejournal.R
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

class EntryViewModel(dateString: String, private val repository: EntryRepository, application: Application) : AndroidViewModel(application) {

    val entry: LiveData<Entry>
    val entryContent = ObservableField<String>("")

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val date: LocalDate = dateString.toLocalDate()
    private val inspiration = application.resources.getStringArray(R.array.inspirations).random()

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
            today -> getApplication<Application>().resources.getString(R.string.today)
            today.minusDays(1) -> getApplication<Application>().resources.getString(R.string.yesterday)
            else -> date.toFullString()
        }
    }

    fun getHintString(): String {
        val today = LocalDate.now()
        return when (date) {
            today -> getApplication<Application>().resources.getString(R.string.what_are_you_thankful_for)
            else -> getApplication<Application>().resources.getString(R.string.what_were_you_thankful_for)
        }
    }

    fun getInspirationString(): String {
        return inspiration
    }

    fun getThankfulString(): String {
        return if (date == LocalDate.now()) {
            getApplication<Application>().resources.getString(R.string.iam)
        } else {
            getApplication<Application>().resources.getString(R.string.iwas)
        }
    }

    fun getShareContent(): String {
        return "${getDateString()} ${getThankfulString()} ${entryContent.get()?.decapitalize()}"
    }

    private fun String.decapitalize(): String {
        if (this.isEmpty()) {
            return this
        }
        val chars = this.toCharArray()
        chars[0] = Character.toLowerCase(chars[0])
        return String(chars)
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
