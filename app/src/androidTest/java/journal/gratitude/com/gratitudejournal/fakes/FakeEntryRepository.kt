package journal.gratitude.com.gratitudejournal.fakes

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.PagingData
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

class FakeEntryRepository @Inject constructor() : EntryRepository {

    //mock of database
    var entriesDatabase: LinkedHashMap<LocalDate, Entry> = LinkedHashMap()

    override suspend fun getEntry(date: LocalDate): Entry? {
        return entriesDatabase[date] ?: Entry(date, "")
    }

    override suspend fun getEntries(): List<Entry> {
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }
        return list
    }

    override fun getEntriesFlow(): Flow<List<Entry>> {
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }
        return flow {
            emit(list)
        }
    }

    override fun getWrittenDates(): LiveData<List<LocalDate>> {
        val liveData = MutableLiveData<List<LocalDate>>()
        liveData.value = entriesDatabase.keys.toList()

        return liveData
    }

    override suspend fun addEntry(entry: Entry) {
        entriesDatabase[entry.entryDate] = entry
    }

    override suspend fun addEntries(entries: List<Entry>) {
        for (entry in entries) {
            entriesDatabase[entry.entryDate] = entry
        }
    }

    override fun searchEntries(query: String): Flow<PagingData<Entry>> {
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }

        return if (query == "query with no result") {
            flow {
                emit(PagingData.from(emptyList<Entry>()))
            }
        } else {
            val results = listOf(Entry(LocalDate.now(), "Today's content"), Entry(LocalDate.of(2019, 11, 29), "Happy birthday, Alison!"))
            flow {
                emit(PagingData.from(results))
            }
        }
    }

    override suspend fun search(query: String): List<Entry> {
        TODO("Not yet implemented")
    }

}