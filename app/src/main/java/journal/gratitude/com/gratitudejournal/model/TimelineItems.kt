package journal.gratitude.com.gratitudejournal.model

import androidx.room.Entity
import androidx.room.PrimaryKey
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
        fun isMilestone(number: Int): Boolean {
            return number == 5 || number == 10 || (number % 25) == 0
        }

        fun create(number: Int): Milestone {
            require(isMilestone(number))

            return Milestone(number, number.toString())
        }

    }
}

