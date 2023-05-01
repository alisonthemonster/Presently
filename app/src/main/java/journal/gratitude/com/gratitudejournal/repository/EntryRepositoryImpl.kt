package journal.gratitude.com.gratitudejournal.repository

import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate
import javax.inject.Inject

class EntryRepositoryImpl @Inject constructor(private val entryDao: EntryDao) : EntryRepository {

    override suspend fun getEntry(date: LocalDate): Entry {
        return entryDao.getEntry(date)
    }

    override fun getEntriesFlow(): Flow<List<Entry>> {
        return entryDao.getEntriesFlow()
    }

    override suspend fun getEntries(): List<Entry> = withContext(Dispatchers.IO) {
        entryDao.getEntries()
    }

    override suspend fun addEntry(entry: Entry): Int = withContext(Dispatchers.IO) {
        if (entry.entryContent.isEmpty()) {
            entryDao.delete(entry)
        } else {
            entryDao.insertEntry(entry)
        }
        entryDao.getNumberOfEntries()
    }

    override suspend fun addEntries(entries: List<Entry>) = withContext(Dispatchers.IO) {
        entryDao.insertEntries(entries)
    }

    override suspend fun search(query: String): List<Entry> {
        val escapedQuery = query.replace("\"", "")
        val wildcardQuery = String.format("*%s*", escapedQuery)
        return entryDao.search(wildcardQuery)
    }
}
