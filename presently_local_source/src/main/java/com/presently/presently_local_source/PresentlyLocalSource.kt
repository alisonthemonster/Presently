package com.presently.presently_local_source

import androidx.paging.PagingData
import com.presently.presently_local_source.model.Entry
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

interface PresentlyLocalSource {

    fun getEntriesFlow(): Flow<List<Entry>>

    suspend fun getEntries(): List<Entry>

    suspend fun getEntry(date: LocalDate): Entry?

    fun searchEntries(query: String): Flow<PagingData<Entry>>

    suspend fun addEntry(entry: Entry)

    suspend fun addEntries(entries: List<Entry>)

    suspend fun getWrittenDates(): List<LocalDate>

}