package journal.gratitude.com.gratitudejournal.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

interface EntryRepository {

    fun getEntry(date: LocalDate): LiveData<Entry>

    fun getAllEntries(): LiveData<List<Entry>>

    fun getWrittenDates(): LiveData<List<LocalDate>>

    suspend fun addEntry(entry: Entry)

    suspend fun addEntries(entries: List<Entry>)

    fun searchEntries(query: String): LiveData<PagedList<Entry>>
}