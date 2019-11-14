package journal.gratitude.com.gratitudejournal.room

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.room.*
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntries(): LiveData<List<Entry>>

    @Query("SELECT entryDate FROM entries ORDER BY datetime(entryDate) DESC")
    fun getWrittenDates(): LiveData<List<LocalDate>>

    @Query("SELECT * FROM entries WHERE entryDate = :date")
    fun getEntry(date: LocalDate): LiveData<Entry>

    @Query("SELECT COUNT(*) FROM entries WHERE entryDate = :date")
    fun getCountForDate(date: LocalDate): LiveData<Int>

    @Delete
    fun delete(entry: Entry)

    @Query("SELECT entries.* FROM entries JOIN entriesFts ON (entries.`rowid` = entriesFts.`rowid`) WHERE entriesFts MATCH :query ORDER BY datetime(entriesFts.entryDate) DESC")
    fun searchAllEntries(query: String): DataSource.Factory<Int, Entry>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntry(entry: Entry)

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntries(entry: List<Entry>)

}