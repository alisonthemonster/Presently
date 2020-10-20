package journal.gratitude.com.gratitudejournal.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagedList
import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface EntryRepository {

    fun getEntry(date: LocalDate): LiveData<Entry>

    suspend fun getEntriesFlow(): Flow<List<Entry>>

    suspend fun getEntries(): List<Entry>

    fun getWrittenDates(): LiveData<List<LocalDate>>

    suspend fun addEntry(entry: Entry)

    suspend fun addEntries(entries: List<Entry>)

    fun searchEntries(query: String): LiveData<PagedList<Entry>>
}