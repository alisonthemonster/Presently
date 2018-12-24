package journal.gratitude.com.gratitudejournal.repository

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.room.EntryDao
import org.threeten.bp.LocalDate

class EntryRepository(private val entryDao: EntryDao) {

    fun getEntry(date: LocalDate): LiveData<Entry> {
        return entryDao.getEntry(date)
    }

    fun getAllEntries(): LiveData<List<Entry>> {
        return entryDao.getEntries()
    }

    @WorkerThread
    suspend fun addEntry(entry: Entry) {
        entryDao.insertEntry(entry)
    }


}