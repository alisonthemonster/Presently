package journal.gratitude.com.gratitudejournal.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.paging.*
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate
import javax.inject.Inject


class EntryRepositoryImpl @Inject constructor(private val entryDao: EntryDao): EntryRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getEntry(date: LocalDate): LiveData<Entry> {
        return entryDao.getEntry(date)
    }

    override suspend fun getEntrySuspend(date: LocalDate): Entry {
        return entryDao.getEntrySuspend(date)
    }

    override suspend fun getEntriesFlow(): Flow<List<Entry>> {
        return entryDao.getEntriesFlow()
    }

    override suspend fun getEntries(): List<Entry> {
        return entryDao.getEntries()
    }

    override fun getWrittenDates(): LiveData<List<LocalDate>> {
        return entryDao.getWrittenDates()
    }

    @WorkerThread
    override suspend fun addEntry(entry: Entry) {
        if (entry.entryContent.isEmpty()) {
            entryDao.delete(entry)
        } else {
            entryDao.insertEntry(entry)
        }
    }

    @WorkerThread
    override suspend fun addEntries(entries: List<Entry>) {
        entryDao.insertEntries(entries)
    }

    override fun searchEntries(query: String): Flow<PagingData<Entry>> {
        val escapedQuery = query.replace("\"", "")
        val wildcardQuery = String.format("*%s*", escapedQuery)

        val searchAllEntries = entryDao.searchAllEntries(wildcardQuery)
        return Pager(
            PagingConfig(pageSize = PAGE_SIZE)
        ) {
            searchAllEntries
        }.flow

    }

}