package journal.gratitude.com.gratitudejournal.repository

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import android.util.Log
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.room.EntryDao
import org.threeten.bp.LocalDate
import journal.gratitude.com.gratitudejournal.util.OpenForTesting

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


}