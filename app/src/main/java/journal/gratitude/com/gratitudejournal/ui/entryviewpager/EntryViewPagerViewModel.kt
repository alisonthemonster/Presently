package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import androidx.lifecycle.viewModelScope
import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import journal.gratitude.com.gratitudejournal.mavericks_utils.AssistedViewModelFactory
import journal.gratitude.com.gratitudejournal.mavericks_utils.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.util.appendTodayAndYesterday
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class EntryViewPagerViewModel @AssistedInject constructor(
    @Assisted val initialState: EntryViewPagerState, private val repository: EntryRepository
) : MavericksViewModel<EntryViewPagerState>(initialState) {

    init {
        viewModelScope.launch {
            repository.getEntriesFlow().collect { list ->
                val listWithAppendedTodayAndYesterday = appendTodayAndYesterday(list)
                val entriesList = addSelectedDate(listWithAppendedTodayAndYesterday)
                val entriesCount = getEntriesCount(entriesList)
                setState {
                    copy(entriesList = entriesList, numEntries = entriesCount)
                }
            }
        }
    }

    //In case user select a date from calender
    private fun addSelectedDate(list: List<Entry>): List<Entry> {
        val doesSelectedValueExist =
            list.firstOrNull() { it.entryDate == initialState.selectedDate }
        return if (doesSelectedValueExist != null)
            list
        else {
            val updatedList = list.toMutableList()
            updatedList.add(Entry(initialState.selectedDate, ""))
            updatedList.sortByDescending { it.entryDate }
            updatedList
        }
    }

    private fun getEntriesCount(currentList: List<Entry>): Int {
        var numEntries = 0
        for (entry in currentList) {
            if (entry.entryContent.isNotEmpty()) numEntries++
        }
        return numEntries
    }


    @AssistedFactory
    interface Factory : AssistedViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> {
        override fun create(state: EntryViewPagerState): EntryViewPagerViewModel
    }

    companion object :
        MavericksViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> by hiltMavericksViewModelFactory(
            EntryViewPagerViewModel::class.java
        )

}
