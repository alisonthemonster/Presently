package journal.gratitude.com.gratitudejournal.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.LocalDate

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey val entryDate: LocalDate,
    val entryContent: String
)