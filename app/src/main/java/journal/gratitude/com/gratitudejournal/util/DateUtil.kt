package journal.gratitude.com.gratitudejournal.util

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

fun String.toLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return org.threeten.bp.LocalDate.parse(this, formatter)
}

fun LocalDate.toDatabaseString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return this.format(formatter)
}

fun LocalDate.toFullString(): String {
    val formatter = DateTimeFormatter.ofPattern("LLLL dd, yyyy")
    return this.format(formatter)
}