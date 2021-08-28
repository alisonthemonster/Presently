package com.presently.presently_local_source

import androidx.paging.*
import com.presently.presently_local_source.database.EntryDao
import com.presently.presently_local_source.model.Entry
import com.presently.presently_local_source.model.EntryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.threeten.bp.LocalDate
import javax.inject.Inject

class RealPresentlyLocalSource @Inject constructor(private val entryDao: EntryDao) : PresentlyLocalSource {
    companion object {
        private const val PAGE_SIZE = 20
    }

    override suspend fun getEntry(date: LocalDate): Entry? {
        return entryDao.getEntry(date)?.toEntry()
    }

    override fun getEntriesFlow(): Flow<List<Entry>> {
        return entryDao.getEntriesFlow().map { entityList ->
            entityList.map { it.toEntry() }
        }
    }

    override suspend fun getEntries(): List<Entry> {
        return entryDao.getEntries().map { it.toEntry() }
    }

    override suspend fun getWrittenDates(): List<LocalDate> {
        return entryDao.getWrittenDates()
    }

    override suspend fun addEntry(entry: Entry) {
        if (entry.entryContent.isEmpty()) {
            entryDao.delete(entry.toEntryEntity())
        } else {
            entryDao.insertEntry(entry.toEntryEntity())
        }
    }

    override suspend fun addEntries(entries: List<Entry>) {
        entryDao.insertEntries(entries.map { it.toEntryEntity() })
    }

    override fun searchEntries(query: String): Flow<PagingData<Entry>> {
        val escapedQuery = query.replace("\"", "")
        val wildcardQuery = String.format("*%s*", escapedQuery)

        val searchAllEntries = entryDao.searchAllEntries(wildcardQuery)
        return Pager(
            PagingConfig(pageSize = PAGE_SIZE)
        ) {
            searchAllEntries
        }.flow.map { pagingData ->
            pagingData.map { it.toEntry() }
        }
    }

    private fun Entry.toEntryEntity(): EntryEntity {
        return EntryEntity(this.entryDate, this.entryContent)
    }

    private fun EntryEntity.toEntry(): Entry {
        return Entry(this.entryDate, this.entryContent)
    }
}