package journal.gratitude.com.gratitudejournal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import journal.gratitude.com.gratitudejournal.R
import org.threeten.bp.LocalDate

sealed class TimelineItem

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey
    val entryDate: LocalDate,
    val entryContent: String
): TimelineItem()

data class Milestone(val number: Int,
                     val numString: String): TimelineItem() {

    companion object {
        val milestones = arrayOf(5, 10, 25, 50, 100, 150, 200, 250, 300, 365, 400, 450, 500, 550)

        fun create(number: Int): Milestone {
            require(milestones.contains(number))

            return Milestone(number, number.toString())
        }

    }
}

