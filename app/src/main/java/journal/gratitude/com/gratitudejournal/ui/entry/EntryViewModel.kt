package journal.gratitude.com.gratitudejournal.ui.entry

import android.app.Application
import androidx.databinding.Observable
import androidx.databinding.Observable.OnPropertyChangedCallback
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.*
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
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class EntryViewModel @Inject constructor(private val repository: EntryRepository, application: Application) :
    AndroidViewModel(application) {

    val entry: LiveData<Entry>
    private val dateLiveData = MutableLiveData<LocalDate>()
    val entryContent = ObservableField<String>("")
    val isEmpty = ObservableBoolean(true)

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)
    private val inspiration = application.resources.getStringArray(R.array.inspirations).random()
    private var promptString = ""
    private val promptsArray = application.resources.getStringArray(R.array.prompts)
    private val prompts = getPromptsQueue(promptsArray)

    init {

        entryContent.addOnPropertyChangedCallback(object : OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if (sender == entryContent) {
                    val content = entryContent.get() ?: ""
                    if (content.isEmpty()) {
                        isEmpty.set(true)
                    } else {
                        isEmpty.set(false)
                    }
                }
            }
        })

        entry = MediatorLiveData()

        entry.addSource(dateLiveData) { date ->
            entry.addSource(repository.getEntry(date)) { data ->
                if (data != null) {
                    entryContent.set(data.entryContent)
                }
                entry.value = data
            }
        }

    }

    fun setDate(passedInDate: String) {
        val date = passedInDate.toLocalDate()
        dateLiveData.value = date
    }

    fun addNewEntry() = scope.launch(Dispatchers.IO) {
        val date = dateLiveData.value ?: throw IOException("Date was not provided")
        val entry = Entry(date, entryContent.get() ?: "")
        repository.addEntry(entry)
    }

    fun getDateString(): String {
        val date = dateLiveData.value ?: throw IOException("Date was not provided")
        val today = LocalDate.now()
        return when (date) {
            today -> getApplication<Application>().resources.getString(R.string.today)
            today.minusDays(1) -> getApplication<Application>().resources.getString(R.string.yesterday)
            else -> date.toFullString()
        }
    }

    fun getHintString(): String {
        val date = dateLiveData.value ?: throw IOException("Date was not provided")
        return if (promptString.isEmpty()) {
            val today = LocalDate.now()
            when (date) {
                today -> getApplication<Application>().resources.getString(R.string.what_are_you_thankful_for)
                else -> getApplication<Application>().resources.getString(R.string.what_were_you_thankful_for)
            }
        } else {
            promptString
        }
    }

    fun getRandomPromptHintString() {
        promptString = getPrompt()
    }

    fun getInspirationString(): String {
        return inspiration
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

    fun getThankfulString(): String {
        val date = dateLiveData.value ?: throw IOException("Date was not provided")
        return if (date == LocalDate.now()) {
            getApplication<Application>().resources.getString(R.string.iam)
        } else {
            getApplication<Application>().resources.getString(R.string.iwas)
        }
    }

    private fun getPromptsQueue(prompts: Array<String>): LinkedList<String> {
        val shuffled = prompts.toMutableList().shuffled()
        val queue = LinkedList<String>()
        for (prompt in shuffled) {
            queue.add(prompt)
        }
        return queue
    }

    private fun getPrompt(): String {
        val next = prompts.remove()
        prompts.add(next)
        return next
    }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}
