package journal.gratitude.com.gratitudejournal.room

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY datetime(entryDate)")
    fun getEntries(): LiveData<List<Entry>>

    @Query("SELECT * FROM entries WHERE entryDate LIKE :date")
    fun getEntry(date: LocalDate): LiveData<Entry>

    @Insert(
        onConflict = OnConflictStrategy.REPLACE
    )
    fun insertEntry(entry: Entry)
}