package journal.gratitude.com.gratitudejournal.ui.entry

import android.arch.lifecycle.ViewModel
import android.databinding.ObservableBoolean
import android.databinding.ObservableField
import journal.gratitude.com.gratitudejournal.util.SingleLiveEvent
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDate

class EntryViewModel(dateString: String) : ViewModel() {

    private val date = dateString.toLocalDate()

    val entryContent = ObservableField<String>("hellooooo")
    private val isSaved = ObservableBoolean()
    private val isSaving = ObservableBoolean()

    val errorLiveEvent = SingleLiveEvent<String>()

    fun getDateString(): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Today" //TODO move to resources
            today.minusDays(1) -> "Yesterday"
            else -> date.toString()
        }
    }

    fun fetchEntryContent() {
        val content = "OMG WE GOT SOME STUFF. What a time to be alive"

        //TODO get real content

        isSaved.set(true)
        entryContent.set(content)
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


}
