package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import com.presently.mavericks_utils.AssistedViewModelFactory
import com.presently.mavericks_utils.hiltMavericksViewModelFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import kotlin.coroutines.CoroutineContext

class EntryViewPagerViewModel @AssistedInject constructor(
    @Assisted val initialState: EntryViewPagerState, private val repository: EntryRepository
) : MavericksViewModel<EntryViewPagerState>(initialState) {

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        scope.launch {
            repository.getEntriesFlow().collect { list ->
                val today = LocalDate.now()
                val yesterday = LocalDate.now().minusDays(1)
                val itemsList = mutableListOf<Entry>()
                when {
                    list.isEmpty() -> {
                        itemsList.add(Entry(today, ""))
                        itemsList.add(Entry(yesterday, ""))
                    }

                    list.size < 2 -> {
                        itemsList.addAll(list)
                        if (list[0].entryDate != today) {
                            itemsList.add(0, Entry(today, ""))
                        }
                        if (list[0].entryDate != yesterday) {
                            itemsList.add(1, Entry(yesterday, ""))
                        }
                    }

                    else -> {
                        val latest = list[0]
                        itemsList.addAll(list)
                        if (latest.entryDate != today) {
                            //they don't have the latest
                            itemsList.add(0, Entry(today, ""))
                        }
                        if (itemsList[1].entryDate != yesterday) {
                            itemsList.add(1, Entry(yesterday, ""))
                        }
                    }
                }
                val entriesList = addSelectedDate(itemsList)
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
            if (entry is Entry && entry.entryContent.isNotEmpty()) numEntries++
        }
        return numEntries
    }


    @AssistedFactory
    interface Factory : AssistedViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> {
        override fun create(state: EntryViewPagerState): EntryViewPagerViewModel
    }

    companion object :
        MavericksViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> by hiltMavericksViewModelFactory()

}