package journal.gratitude.com.gratitudejournal.ui.entry_viewpager

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
    @Assisted initialState: EntryViewPagerState, private val repository: EntryRepository
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
                when {
                    list.isEmpty() -> {
                        setState {
                            copy(
                                entriesList = listOf(
                                    Entry(today, ""), Entry(yesterday, "")
                                )
                            )
                        }
                    }
                    list.size < 2 -> {
                        //user has only ever written one day
                        val newList = mutableListOf<Entry>()
                        newList.addAll(list)
                        if (list[0].entryDate != today) {
                            newList.add(0, Entry(today, ""))
                        }
                        if (list[0].entryDate != yesterday) {
                            newList.add(1, Entry(yesterday, ""))
                        }
                        setState { copy(entriesList = newList) }
                    }

                    else -> {
                        val latest = list[0]
                        val listWithHints = mutableListOf<Entry>()
                        listWithHints.addAll(list)
                        if (latest.entryDate != today) {
                            //they don't have the latest
                            listWithHints.add(0, Entry(today, ""))
                        }
                        if (listWithHints[1].entryDate != yesterday) {
                            listWithHints.add(1, Entry(yesterday, ""))
                        }

                        val listWithHintsAndMilestones = mutableListOf<Entry>()

                        var numEntries = 0
                        for (index in listWithHints.size - 1 downTo 0) {
                            listWithHintsAndMilestones.add(0, listWithHints[index])
                            if (listWithHints[index].entryContent.isNotEmpty()) {
                                numEntries++
                            }
                        }
                        setState {
                            copy(entriesList = listWithHintsAndMilestones)
                        }
                    }
                }
            }
        }
    }


    @AssistedFactory
    interface Factory : AssistedViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> {
        override fun create(state: EntryViewPagerState): EntryViewPagerViewModel
    }

    companion object :
        MavericksViewModelFactory<EntryViewPagerViewModel, EntryViewPagerState> by hiltMavericksViewModelFactory()

}