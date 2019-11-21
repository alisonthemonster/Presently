package journal.gratitude.com.gratitudejournal.ui.search

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.paging.PagedList
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.ui.bindingadapter.Visibility
import javax.inject.Inject

class SearchViewModel @Inject constructor(val repository: EntryRepository) : ViewModel() {

    private val queryLiveData = MutableLiveData<String>()
    private val searchResult: LiveData<LiveData<PagedList<Entry>>> = Transformations.map(queryLiveData) {
        repository.searchEntries(it)
    }
    val results: LiveData<PagedList<Entry>> = Transformations.switchMap(searchResult) { it }


    fun search(queryString: String) {
        queryLiveData.postValue(queryString)
    }

    @Visibility
    fun noResults(): Int {
        return if (results.value?.size == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}
