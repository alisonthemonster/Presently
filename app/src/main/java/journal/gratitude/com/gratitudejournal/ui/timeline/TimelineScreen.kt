package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarView
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDate
import kotlinx.coroutines.flow.StateFlow

object TimelineScreenTags {
    const val ROOT = "timeline_root"
    const val LIST = "timeline_list"
    const val SEARCH = "timeline_search"
    const val OVERFLOW = "timeline_overflow"
    const val CALENDAR_BUTTON = "timeline_calendar_button"
    const val CALENDAR = "timeline_calendar"

    fun entry(date: String) = "timeline_entry_$date"
    fun milestone(number: Int) = "timeline_milestone_$number"
}

@Composable
fun TimelineScreen(
    state: StateFlow<TimelineUiState>,
    onSearchClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onContactClick: () -> Unit,
    onTimelineEntryClick: (TimelineEntryRowState) -> Unit,
    onCalendarClick: () -> Unit,
    onCalendarClose: () -> Unit,
    onCalendarDateClick: (LocalDate, Boolean, Int) -> Unit
) {
    val uiState by state.collectAsStateWithLifecycle()
    TimelineScreenContent(
        state = uiState,
        onSearchClick = onSearchClick,
        onOverflowClick = onOverflowClick,
        onOverflowDismiss = onOverflowDismiss,
        onSettingsClick = onSettingsClick,
        onContactClick = onContactClick,
        onTimelineEntryClick = onTimelineEntryClick,
        onCalendarClick = onCalendarClick,
        onCalendarClose = onCalendarClose,
        onCalendarDateClick = onCalendarDateClick
    )
}

@Composable
fun TimelineScreenContent(
    state: TimelineUiState,
    onSearchClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onContactClick: () -> Unit,
    onTimelineEntryClick: (TimelineEntryRowState) -> Unit,
    onCalendarClick: () -> Unit,
    onCalendarClose: () -> Unit,
    onCalendarDateClick: (LocalDate, Boolean, Int) -> Unit
) {
    val theme = LocalPresentlyTheme.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag(TimelineScreenTags.ROOT),
        color = theme.timelineBackground
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                TimelineToolbar(
                    onSearchClick = onSearchClick,
                    onOverflowClick = onOverflowClick,
                    onOverflowDismiss = onOverflowDismiss,
                    onSettingsClick = onSettingsClick,
                    onContactClick = onContactClick,
                    isOverflowExpanded = state.isOverflowMenuExpanded
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(TimelineScreenTags.LIST)
                ) {
                    items(
                        items = state.items,
                        key = { item ->
                            when (item) {
                                is TimelineEntryRowState -> item.date.toString()
                                is TimelineMilestoneRowState -> "milestone-${item.number}"
                            }
                        }
                    ) { item ->
                        when (item) {
                            is TimelineEntryRowState -> TimelineEntryRow(
                                state = item,
                                onClick = { onTimelineEntryClick(item) }
                            )
                            is TimelineMilestoneRowState -> TimelineMilestoneRow(state = item)
                        }
                    }
                }
            }

            if (state.isCalendarVisible) {
                AndroidView(
                    factory = { context ->
                        EntryCalendarView(context).apply {
                            id = R.id.entry_calendar
                            visibility = View.VISIBLE
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 64.dp)
                        .testTag(TimelineScreenTags.CALENDAR),
                    update = { calendarView ->
                        calendarView.setWrittenDates(state.writtenDates)
                        calendarView.setDayClickedListener(
                            object : journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarListener {
                                override fun onDateClicked(
                                    date: java.util.Date,
                                    isNewDate: Boolean,
                                    numberOfEntries: Int
                                ) {
                                    onCalendarDateClick(
                                        date.toLocalDate(),
                                        isNewDate,
                                        numberOfEntries
                                    )
                                }

                                override fun onCloseClicked() {
                                    onCalendarClose()
                                }
                            }
                        )
                    }
                )
            }

            FloatingActionButton(
                onClick = onCalendarClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag(TimelineScreenTags.CALENDAR_BUTTON),
                shape = CircleShape,
                containerColor = theme.fab,
                contentColor = theme.fabText
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_calendar),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
private fun TimelineToolbar(
    isOverflowExpanded: Boolean,
    onSearchClick: () -> Unit,
    onOverflowClick: () -> Unit,
    onOverflowDismiss: () -> Unit,
    onSettingsClick: () -> Unit,
    onContactClick: () -> Unit
) {
    val theme = LocalPresentlyTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.toolbar)
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_search),
            contentDescription = stringResource(R.string.search),
            modifier = Modifier
                .padding(start = 24.dp)
                .size(30.dp)
                .clickable(onClick = onSearchClick)
                .testTag(TimelineScreenTags.SEARCH)
        )

        Image(
            painter = painterResource(R.drawable.ic_title),
            contentDescription = stringResource(R.string.presently),
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
                .height(56.dp)
                .padding(13.dp),
            contentScale = ContentScale.Fit
        )

        Box {
            Image(
                painter = painterResource(R.drawable.ic_overflow),
                contentDescription = stringResource(R.string.settings),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(width = 40.dp, height = 35.dp)
                    .padding(4.dp)
                    .clickable(onClick = onOverflowClick)
                    .testTag(TimelineScreenTags.OVERFLOW)
            )

            DropdownMenu(
                expanded = isOverflowExpanded,
                onDismissRequest = onOverflowDismiss
            ) {
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.notification_settings)) },
                    onClick = onSettingsClick
                )
                DropdownMenuItem(
                    text = { Text(text = stringResource(R.string.contact_us)) },
                    onClick = onContactClick
                )
            }
        }
    }
}

@Composable
private fun TimelineEntryRow(
    state: TimelineEntryRowState,
    onClick: () -> Unit
) {
    val theme = LocalPresentlyTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(TimelineScreenTags.entry(state.date.toString()))
            .padding(bottom = if (state.isLastItem) 14.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 32.dp)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(theme.timelineLine)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 26.5.dp, top = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimelineCircle(
                    filled = state.isCurrentDate
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = state.dateText,
                    color = theme.timelineHeader,
                    fontFamily = PresentlyFontFamilies.accent,
                    fontSize = 20.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 55.dp, top = 8.dp)
            ) {
                Text(
                    text = state.content,
                    color = theme.timelineBody,
                    fontFamily = PresentlyFontFamilies.body,
                    fontSize = 16.sp,
                    minLines = 3,
                    maxLines = state.maxLines,
                    overflow = TextOverflow.Ellipsis
                )

                if (state.emptyHint != null) {
                    Text(
                        text = stringResource(state.emptyHint),
                        color = theme.timelineHint,
                        fontFamily = PresentlyFontFamilies.body,
                        fontSize = 16.sp
                    )
                }
            }

            if (state.isLastItem) {
                Image(
                    painter = painterResource(theme.timelineIconRes),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 80.dp, top = 16.dp, end = 80.dp, bottom = 32.dp)
                        .height(80.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }

        if (state.isLastItem) {
            TimelineCircle(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = 26.5.dp),
                filled = true
            )
        }
    }
}

@Composable
private fun TimelineMilestoneRow(state: TimelineMilestoneRowState) {
    val theme = LocalPresentlyTheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(TimelineScreenTags.milestone(state.number))
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clipToBounds()
        ) {
            Box(
                modifier = Modifier
                    .offset(x = 32.dp)
                    .fillMaxHeight()
                    .width(3.dp)
                    .background(theme.timelineLine)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 26.5.dp, top = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                TimelineCircle(filled = false)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = state.numberText,
                    color = theme.timelineHeader,
                    fontFamily = PresentlyFontFamilies.accent,
                    fontSize = 36.sp
                )
                Text(
                    text = stringResource(R.string.days_of),
                    color = theme.timelineHeader,
                    fontFamily = PresentlyFontFamilies.accent,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Image(
                painter = painterResource(theme.timelineIconRes),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 80.dp, top = 8.dp, end = 80.dp, bottom = 32.dp)
                    .height(80.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun TimelineCircle(
    modifier: Modifier = Modifier,
    filled: Boolean
) {
    val theme = LocalPresentlyTheme.current
    val circleModifier = modifier
        .size(14.dp)

    if (filled) {
        Box(
            modifier = circleModifier.background(
                color = theme.timelineLine,
                shape = androidx.compose.foundation.shape.CircleShape
            )
        )
    } else {
        Box(
            modifier = circleModifier
                .background(
                    color = theme.timelineBackground,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
                .border(
                    width = 3.dp,
                    color = theme.timelineLine,
                    shape = androidx.compose.foundation.shape.CircleShape
                )
        )
    }
}
