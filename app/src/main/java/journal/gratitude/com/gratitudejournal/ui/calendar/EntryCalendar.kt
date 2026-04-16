package journal.gratitude.com.gratitudejournal.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import org.threeten.bp.LocalDate
import java.time.DayOfWeek
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.time.LocalDate as JavaLocalDate

object EntryCalendarTags {
    const val CLOSE_BUTTON = "entry_calendar_close"
    const val RANDOM_BUTTON = "entry_calendar_random"

    fun day(date: LocalDate) = "entry_calendar_day_$date"
}

@Composable
fun EntryCalendar(
    writtenDates: List<LocalDate>,
    firstDayOfWeek: DayOfWeek,
    onDateClick: (LocalDate, isNewDate: Boolean, numberOfEntries: Int) -> Unit,
    onCloseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalPresentlyTheme.current
    val writtenDatesSet = remember(writtenDates) {
        writtenDates.mapTo(HashSet()) { it.toJavaLocalDate() }
    }
    val today = remember { JavaLocalDate.now() }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(120) }
    val daysOfWeek = remember(firstDayOfWeek) { daysOfWeek(firstDayOfWeek) }
    val displayLocale = Locale.getDefault()
    val monthTitleFormatter = remember(displayLocale) {
        DateTimeFormatter.ofPattern("LLLL yyyy", displayLocale)
    }
    val calendarState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = currentMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val visibleMonthTitle = remember(calendarState, monthTitleFormatter) {
        derivedStateOf {
            calendarState.firstVisibleMonth.yearMonth.atDay(1).format(monthTitleFormatter)
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onCloseClick)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = theme.toolbar),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = visibleMonthTitle.value,
                        color = theme.toolbarItem,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                    Text(
                        text = stringResource(R.string.close),
                        color = theme.toolbarItem,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable(onClick = onCloseClick)
                            .padding(8.dp)
                            .testTag(EntryCalendarTags.CLOSE_BUTTON)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    daysOfWeek.forEach { dayOfWeek ->
                        Text(
                            text = dayOfWeek.getDisplayName(TextStyle.NARROW, displayLocale),
                            color = theme.toolbarItem,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalCalendar(
                    state = calendarState,
                    dayContent = { day ->
                        CalendarDay(
                            day = day,
                            today = today,
                            isWritten = writtenDatesSet.contains(day.date),
                            dayTag = EntryCalendarTags.day(day.date.toThreetenLocalDate()),
                            onDateClick = { clickedDate ->
                                val threetenDate = clickedDate.toThreetenLocalDate()
                                onDateClick(
                                    threetenDate,
                                    !writtenDates.contains(threetenDate),
                                    writtenDates.size
                                )
                            }
                        )
                    }
                )

                if (writtenDates.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = stringResource(R.string.random),
                            color = theme.toolbarItem,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .clickable {
                                    val randomDate = writtenDates.randomOrNull()
                                    randomDate?.let {
                                        onDateClick(it, false, writtenDates.size)
                                    }
                                }
                                .padding(8.dp)
                                .testTag(EntryCalendarTags.RANDOM_BUTTON)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: CalendarDay,
    today: JavaLocalDate,
    isWritten: Boolean,
    dayTag: String,
    onDateClick: (JavaLocalDate) -> Unit
) {
    val theme = LocalPresentlyTheme.current
    val isInMonth = day.position == DayPosition.MonthDate
    val isFuture = day.date.isAfter(today)
    val isToday = day.date == today

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .testTag(dayTag)
            .then(
                if (isInMonth && !isFuture) Modifier.clickable { onDateClick(day.date) }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(if (isToday) 24.dp else 20.dp)
                    .then(
                        if (isToday) Modifier.background(theme.timelineHint, CircleShape)
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day.date.dayOfMonth.toString(),
                    color = when {
                        isFuture && isInMonth -> theme.toolbarItem.copy(alpha = 0.4f)
                        isInMonth -> theme.toolbarItem
                        else -> theme.toolbarItem.copy(alpha = 0.2f)
                    },
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            if (isWritten && isInMonth) {
                Box(
                    modifier = Modifier
                        .size(3.dp)
                        .padding(top = 1.dp)
                        .background(theme.toolbarItem, CircleShape)
                )
            }
        }
    }
}

private fun LocalDate.toJavaLocalDate(): JavaLocalDate =
    JavaLocalDate.of(year, monthValue, dayOfMonth)

private fun JavaLocalDate.toThreetenLocalDate(): LocalDate =
    LocalDate.of(year, monthValue, dayOfMonth)
