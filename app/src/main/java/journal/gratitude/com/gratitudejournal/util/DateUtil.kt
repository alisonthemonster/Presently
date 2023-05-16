package journal.gratitude.com.gratitudejournal.util

import kotlinx.datetime.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun LocalDate.toThreeTenBpLocalDate(): org.threeten.bp.LocalDate {
    val year = this.year
    val month = this.month.value
    val day = this.dayOfMonth
    return org.threeten.bp.LocalDate.of(year, month, day)
}

/**
 * Returns the {yyyy-MM-dd} string of the LocalDate
 */
fun LocalDate.toDatabaseString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return this.toThreeTenBpLocalDate().format(formatter)
}

fun LocalDate.toFullString(): String {
    val localizedTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.LONG)

    return localizedTimeFormatter.format(this.toThreeTenBpLocalDate())
}

fun LocalDate.toStringWithDayOfWeek(): String {
    val localizedTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(FormatStyle.FULL)

    return localizedTimeFormatter.format(this.toThreeTenBpLocalDate())
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
