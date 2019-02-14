package journal.gratitude.com.gratitudejournal.room

import androidx.lifecycle.LiveData
import androidx.room.*
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY datetime(entryDate) DESC")
    fun getEntries(): LiveData<List<Entry>>

    @Query("SELECT * FROM entries WHERE entryDate LIKE :date")
    fun getEntry(date: LocalDate): LiveData<Entry>

    @Delete
    fun delete(entry: Entry)

    @Query("DELETE FROM entries WHERE entryDate = :date")
    fun deleteByDate(date: String)

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntry(entry: Entry)
}