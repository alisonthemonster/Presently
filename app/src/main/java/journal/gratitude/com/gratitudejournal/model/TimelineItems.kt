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
        val milestones = setOf(5, 10, 25, 50, 75, 100, 125, 150, 175, 200, 225, 250, 275, 300, 325, 365, 400, 425, 450, 475, 500, 525, 550, 575, 600)

        fun create(number: Int): Milestone {
            require(milestones.contains(number))

            return Milestone(number, number.toString())
        }

    }
}

