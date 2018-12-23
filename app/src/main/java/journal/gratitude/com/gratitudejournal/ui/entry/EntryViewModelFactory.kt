package journal.gratitude.com.gratitudejournal.ui.entry

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import java.util.*

@Suppress("UNCHECKED_CAST")
class EntryViewModelFactory(
    private val date: String
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EntryViewModel(date) as T
    }
}
