package journal.gratitude.com.gratitudejournal.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.room.EntryDao
import journal.gratitude.com.gratitudejournal.util.OpenForTesting
import org.threeten.bp.LocalDate

@OpenForTesting
class EntryRepository(private val entryDao: EntryDao) {

    fun getEntry(date: LocalDate): LiveData<Entry> {
        return entryDao.getEntry(date)
    }

    fun getAllEntries(): LiveData<List<Entry>> {
        return entryDao.getEntries()
    }

    @WorkerThread
    suspend fun addEntry(entry: Entry) {
        if (entry.entryContent.isEmpty()) {
            entryDao.delete(entry)
        } else {
            entryDao.insertEntry(entry)
        }
    }

    @WorkerThread
    suspend fun addEntries(entries: List<Entry>) {
        entryDao.insertEntries(entries)
    }

    fun searchEntries(query: String): LiveData<List<Entry>> {
        return entryDao.searchAllEntries(query) //TODO look into LivePagedListBuilder
    }

}