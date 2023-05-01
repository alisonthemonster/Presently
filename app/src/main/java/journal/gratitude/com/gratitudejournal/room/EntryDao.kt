package journal.gratitude.com.gratitudejournal.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import journal.gratitude.com.gratitudejournal.model.Entry
import kotlinx.coroutines.flow.Flow
import org.threeten.bp.LocalDate

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntriesFlow(): Flow<List<Entry>>

    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntries(): List<Entry>

    @Query("SELECT COUNT(entryDate) FROM entries")
    suspend fun getNumberOfEntries(): Int

    @Query("SELECT * FROM entries WHERE entryDate = :date")
    suspend fun getEntry(date: LocalDate): Entry

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT entries.* FROM entries JOIN entriesFts ON (entries.`rowid` = entriesFts.`rowid`) WHERE entriesFts MATCH :query ORDER BY datetime(entriesFts.entryDate) DESC")
    suspend fun search(query: String): List<Entry>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    suspend fun insertEntry(entry: Entry)

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntries(entry: List<Entry>)
}
