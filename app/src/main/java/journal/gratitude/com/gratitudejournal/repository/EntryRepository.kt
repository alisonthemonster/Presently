package journal.gratitude.com.gratitudejournal.repository

import androidx.paging.PagingData
import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface EntryRepository {

    suspend fun getEntry(date: LocalDate): Entry?

    fun getEntriesFlow(): Flow<List<Entry>>

    suspend fun getEntries(): List<Entry>

    suspend fun getWrittenDates(): List<LocalDate>

    suspend fun addEntry(entry: Entry)

    suspend fun addEntries(entries: List<Entry>)

    fun searchEntries(query: String): Flow<PagingData<Entry>>
}