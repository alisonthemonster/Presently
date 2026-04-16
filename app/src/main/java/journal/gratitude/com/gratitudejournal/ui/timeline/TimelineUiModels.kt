package journal.gratitude.com.gratitudejournal.ui.timeline

import androidx.annotation.StringRes
import journal.gratitude.com.gratitudejournal.R
import java.time.DayOfWeek
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.util.appendTodayAndYesterday
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

data class TimelineUiState(
    val items: List<TimelineRowState> = emptyList(),
    val writtenDates: List<LocalDate> = emptyList(),
    val isCalendarVisible: Boolean = false,
    val showReminderOnboardingPrompt: Boolean = false,
    val firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
)

sealed interface TimelineRowState

data class TimelineEntryRowState(
    val date: LocalDate,
    val dateText: String,
    val content: String,
    @StringRes val emptyHint: Int?,
    val isCurrentDate: Boolean,
    val isNewEntry: Boolean,
    val numberExistingEntries: Int,
    val maxLines: Int,
    val isLastItem: Boolean
) : TimelineRowState

data class TimelineMilestoneRowState(
    val number: Int,
    val numberText: String
) : TimelineRowState

sealed interface TimelineEffect {
    data object OpenSearch : TimelineEffect
    data object OpenSettings : TimelineEffect
    data object ExitTimeline : TimelineEffect
    data class OpenEntry(
        val clickedDate: LocalDate,
        val isNewEntry: Boolean,
        val numberExistingEntries: Int
    ) : TimelineEffect
}

internal fun List<Entry>.toTimelineRowStates(
    showDayOfWeek: Boolean,
    linesPerEntry: Int
): List<TimelineRowState> {
    val listWithAppendedTodayAndYesterday = appendTodayAndYesterday(this)
    val totalWrittenEntries = listWithAppendedTodayAndYesterday.count { it.entryContent.isNotEmpty() }
    val rows = mutableListOf<TimelineRowState>()
    var numEntries = 0

    for (index in listWithAppendedTodayAndYesterday.size - 1 downTo 0) {
        val entry = listWithAppendedTodayAndYesterday[index]
        rows.add(
            0,
            TimelineEntryRowState(
                date = entry.entryDate,
                dateText = if (showDayOfWeek) {
                    entry.entryDate.toStringWithDayOfWeek()
                } else {
                    entry.entryDate.toFullString()
                },
                content = entry.entryContent,
                emptyHint = if (entry.entryContent.isEmpty()) {
                    if (entry.entryDate == LocalDate.now()) {
                        R.string.what_are_you_thankful_for_today
                    } else {
                        R.string.what_are_you_thankful_for_yesterday
                    }
                } else {
                    null
                },
                isCurrentDate = entry.entryDate == LocalDate.now(),
                isNewEntry = entry.entryContent.isEmpty(),
                numberExistingEntries = totalWrittenEntries,
                maxLines = linesPerEntry,
                isLastItem = false
            )
        )

        if (entry.entryContent.isNotEmpty()) {
            numEntries++
            if (Milestone.isMilestone(numEntries)) {
                rows.add(
                    0,
                    TimelineMilestoneRowState(
                        number = numEntries,
                        numberText = numEntries.toString()
                    )
                )
            }
        }
    }

    val lastEntryIndex = rows.indexOfLast { it is TimelineEntryRowState }
    if (lastEntryIndex >= 0) {
        val lastEntry = rows[lastEntryIndex] as TimelineEntryRowState
        rows[lastEntryIndex] = lastEntry.copy(isLastItem = true)
    }

    return rows
}
