package journal.gratitude.com.gratitudejournal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val entryDate: LocalDate,
    val entryContent: String
)