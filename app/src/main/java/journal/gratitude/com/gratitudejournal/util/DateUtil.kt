package journal.gratitude.com.gratitudejournal.util

import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.*

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

fun Date.getMonthString(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
}

fun Date.getYearString(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.YEAR).toString()
}

fun Date.toLocalDate(): LocalDate {
    return LocalDate.now() //TODO
}