package journal.gratitude.com.gratitudejournal.repository

import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface EntryRepository {

    suspend fun getEntry(date: LocalDate): Entry?

    fun getEntriesFlow(): Flow<List<Entry>>

    suspend fun getEntries(): List<Entry>

    suspend fun addEntry(entry: Entry): Int

    suspend fun addEntries(entries: List<Entry>)

    suspend fun search(query: String): List<Entry>
}