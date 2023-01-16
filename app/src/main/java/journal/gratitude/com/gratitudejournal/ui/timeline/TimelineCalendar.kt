package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import journal.gratitude.com.gratitudejournal.R
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*

@Composable
fun TimelineCalendar(
    modifier: Modifier,
    locale: Locale,
    writtenDates: Set<LocalDate>,
    onDateClicked: (LocalDate) -> Unit,
) {
    val currentDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var startOfMonth by rememberSaveable { mutableStateOf(currentDate.withDayOfMonth(1)) }

    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY,
    )
    Column() {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                startOfMonth = startOfMonth.minusMonths(1)
            }) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.prev_month)
                )
            }
            Text(
                text = startOfMonth.month.toString()
            )
            IconButton(
                onClick = {
                    startOfMonth = startOfMonth.plusMonths(1)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_month)
                )
            }
        }
        LazyVerticalGrid(
            modifier = Modifier.fillMaxWidth(),
            columns = GridCells.Fixed(7),
            content = {
                items(daysOfWeek) { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                        textAlign = TextAlign.Center,
                    )
                }
                items(generateListOfDaysForView(startOfMonth)) { date ->
                    Text(
                        text = date.dayOfMonth.toString(),
                        color = if (date.month != startOfMonth.month) Color.Gray else Color.Black,
                        modifier = Modifier.clickable(date.isBefore(currentDate.plusDays(1))) {
                            onDateClicked(date)
                        },
                        textAlign = TextAlign.Center,
                        fontWeight = if (writtenDates.contains(date)) FontWeight.Black else FontWeight.Normal
                    )
                }
            }
        )
    }
}

private fun generateListOfDaysForView(
    startOfMonth: LocalDate
): List<LocalDate> {
    var dates = mutableListOf<LocalDate>()

    //first add any days of the previous month
    val numberOfDatesToAddBefore = numDaysNeededBeforeStartDate(startOfMonth)
    for (numberOfDays in numberOfDatesToAddBefore downTo 1) {
        dates.add(startOfMonth.minusDays(numberOfDays))
    }

    //then add all days of this month
    for (daysInMonth in 0 until startOfMonth.month.length(false).toLong()) { //todo leap year!
        dates.add(startOfMonth.plusDays(daysInMonth))
    }
    val lastDateOfMonth = dates.last()

    //then add any days of the next month
    val numberOfDatesToAddAfter = numDaysNeededAfterEndDate(lastDateOfMonth)
    for (numberOfDays in 1..numberOfDatesToAddAfter) {
        dates.add(lastDateOfMonth.plusDays(numberOfDays))
    }

    return dates
}

private fun numDaysNeededBeforeStartDate(startOfMonth: LocalDate): Long {
    return when (startOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 0L
        DayOfWeek.TUESDAY -> 1L
        DayOfWeek.WEDNESDAY -> 2L
        DayOfWeek.THURSDAY -> 3L
        DayOfWeek.FRIDAY -> 4L
        DayOfWeek.SATURDAY -> 5L
        DayOfWeek.SUNDAY -> 6L
    }
}

private fun numDaysNeededAfterEndDate(endOfMonth: LocalDate): Long {
    return when (endOfMonth.dayOfWeek) {
        DayOfWeek.MONDAY -> 6L
        DayOfWeek.TUESDAY -> 5L
        DayOfWeek.WEDNESDAY -> 4L
        DayOfWeek.THURSDAY -> 3L
        DayOfWeek.FRIDAY -> 2L
        DayOfWeek.SATURDAY -> 1L
        DayOfWeek.SUNDAY -> 0L
    }
}