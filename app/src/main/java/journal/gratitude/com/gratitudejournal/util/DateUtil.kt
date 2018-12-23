package journal.gratitude.com.gratitudejournal.util

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

fun String.toLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return org.threeten.bp.LocalDate.parse(this, formatter)
}