package journal.gratitude.com.gratitudejournal.fakes

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

class FakeEntryRepository @Inject constructor() : EntryRepository {
    private val entriesDatabase: LinkedHashMap<LocalDate, Entry> = LinkedHashMap()
    init {
        //fake of database
        val date = LocalDate.of(2022, 12, 19)
        entriesDatabase[date] = Entry(date, "A test entry on the 19th of December.")
    }

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

    override suspend fun addEntry(entry: Entry): Int {
        entriesDatabase[entry.entryDate] = entry
        return entriesDatabase.size
    }

    override suspend fun addEntries(entries: List<Entry>) {
        for (entry in entries) {
            entriesDatabase[entry.entryDate] = entry
        }
    }

    override suspend fun search(query: String): List<Entry> {
        if (query == "") return emptyList()
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }

        return list
    }

}