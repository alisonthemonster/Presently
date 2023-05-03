package journal.gratitude.com.gratitudejournal.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "entriesFts")
@Fts4(contentEntity = Entry::class)
data class EntryFts(
    @PrimaryKey
    @ColumnInfo(name = "rowid")
    val rowId: Int,
    val entryDate: LocalDate,
    val entryContent: String
)
