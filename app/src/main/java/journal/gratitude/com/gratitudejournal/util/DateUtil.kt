package journal.gratitude.com.gratitudejournal.util

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.Month
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.format.TextStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun String.toLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return LocalDate.parse(this, formatter)
}

fun LocalDate.toDatabaseString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return this.format(formatter)
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
    val cal = Calendar.getInstance()
    cal.set(this.year, this.monthValue - 1, this.dayOfMonth)
    return cal.time
}
