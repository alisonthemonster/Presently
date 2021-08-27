package com.presently.presently_local_source.database

import androidx.paging.PagingSource
import androidx.room.*
import com.presently.presently_local_source.model.EntryEntity
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntriesFlow(): Flow<List<EntryEntity>>

    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntries(): List<EntryEntity>

    @Query("SELECT entryDate FROM entries ORDER BY datetime(entryDate) DESC")
    suspend fun getWrittenDates(): List<LocalDate>

    @Query("SELECT * FROM entries WHERE entryDate = :date")
    suspend fun getEntry(date: LocalDate): EntryEntity? //todo this can be null right?

    @Delete
    fun delete(entry: EntryEntity)

    @Query("SELECT entries.* FROM entries JOIN entriesFts ON (entries.`rowid` = entriesFts.`rowid`) WHERE entriesFts MATCH :query ORDER BY datetime(entriesFts.entryDate) DESC")
    fun searchAllEntries(query: String): PagingSource<Int, EntryEntity>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntry(entry: EntryEntity)

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntries(entry: List<EntryEntity>)

}