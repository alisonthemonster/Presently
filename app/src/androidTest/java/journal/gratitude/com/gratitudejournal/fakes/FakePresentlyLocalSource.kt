package journal.gratitude.com.gratitudejournal.fakes

import androidx.paging.PagingData
import com.presently.presently_local_source.PresentlyLocalSource
import com.presently.presently_local_source.model.Entry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

class FakePresentlyLocalSource @Inject constructor() : PresentlyLocalSource {

    //mock of database
    private val entriesDatabase: LinkedHashMap<LocalDate, Entry> = LinkedHashMap()

    override suspend fun getEntry(date: LocalDate): Entry {
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

    override suspend fun getWrittenDates(): List<LocalDate> {
        return entriesDatabase.keys.toList()
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

}