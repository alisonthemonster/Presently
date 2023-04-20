package journal.gratitude.com.gratitudejournal.fakes

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

class FakeEntryRepository @Inject constructor() : EntryRepository {

    private val flow = MutableStateFlow(LinkedHashMap<LocalDate, Entry>())

    init {
        //fake of database
        val date = LocalDate.of(2022, 12, 19)
        val entriesDatabase = flow.value
        entriesDatabase[date] = Entry(date, "A test entry on the 19th of December.")
        flow.value = entriesDatabase
    }

    override suspend fun getEntry(date: LocalDate): Entry? {
        val entriesDatabase = flow.value
        return entriesDatabase[date] ?: Entry(date, "")
    }

    override suspend fun getEntries(): List<Entry> {
        val list = mutableListOf<Entry>()
        val entriesDatabase = flow.value
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }
        return list
    }

    override fun getEntriesFlow(): Flow<List<Entry>> {
        return flow.map {
            val list = mutableListOf<Entry>()
            it.forEach { (_, entry) -> list.add(entry) }
            list
        }
    }

    override suspend fun addEntry(entry: Entry): Int {
        val entriesDatabase = flow.value
        entriesDatabase[entry.entryDate] = entry
        flow.value = entriesDatabase
        return entriesDatabase.size
    }

    override suspend fun addEntries(entries: List<Entry>) {
        val entriesDatabase = flow.value
        for (entry in entries) {
            entriesDatabase[entry.entryDate] = entry
        }
        flow.value = entriesDatabase
    }

    override suspend fun search(query: String): List<Entry> {
        if (query == "") return emptyList()
        val entriesDatabase = flow.value
        val list = mutableListOf<Entry>()
        entriesDatabase.forEach { (_, entry) -> list.add(entry) }

        return list
    }

}