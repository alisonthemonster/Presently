package journal.gratitude.com.gratitudejournal.repository

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
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
            Log.d("blerg", "deleting!")
            entryDao.delete(entry)
        } else {
            Log.d("blerg", "adding!")
            entryDao.insertEntry(entry)
        }
    }


}