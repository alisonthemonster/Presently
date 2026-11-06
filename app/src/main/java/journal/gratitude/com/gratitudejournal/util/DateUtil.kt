package journal.gratitude.com.gratitudejournal.util

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.*

private val databaseDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun String.toLocalDate(): LocalDate {
    return LocalDate.parse(this, databaseDateFormatter)
}

fun LocalDate.toDatabaseString(): String {
    return this.format(databaseDateFormatter)
}

fun LocalDate.toFullString(): String {
    val localizedTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)

    return localizedTimeFormatter.format(this)

}

fun LocalDate.toStringWithDayOfWeek(): String {
    val localizedTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)

    return localizedTimeFormatter.format(this)
}

fun Month.toShortMonthString(): String {
    return this.getDisplayName(TextStyle.SHORT, Locale.getDefault())
}

fun Date.toMonthString(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return SimpleDateFormat("LLLL", Locale.getDefault()).format(cal.time)
}

fun Date.getYearString(): String {
    val cal = Calendar.getInstance()
    cal.time = this
    return cal.get(Calendar.YEAR).toString()
}

fun Date.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this.time).atZone(ZoneId.systemDefault()).toLocalDate()
}

fun LocalDate.toDate(): Date {
    return Date.from(atStartOfDay(ZoneId.systemDefault()).toInstant())
}