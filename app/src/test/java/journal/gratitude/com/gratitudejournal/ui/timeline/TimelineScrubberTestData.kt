package journal.gratitude.com.gratitudejournal.ui.timeline

import java.time.LocalDate

internal fun entryRow(date: LocalDate, content: String = "Grateful", isLast: Boolean = false) =
    TimelineEntryRowState(
        date = date,
        dateText = date.toString(),
        content = content,
        emptyHint = null,
        isCurrentDate = false,
        isNewEntry = false,
        numberExistingEntries = 1,
        maxLines = 3,
        isLastItem = isLast
    )

internal fun milestoneRow(number: Int) = TimelineMilestoneRowState(number = number, numberText = number.toString())

/** [count] rows, one per day, newest first, starting at [newest]. */
internal fun dailyRows(count: Int, newest: LocalDate = LocalDate.of(2026, 3, 28)): List<TimelineRowState> =
    List(count) { entryRow(newest.minusDays(it.toLong()), isLast = it == count - 1) }

/** [count] rows, roughly one per 10 days, so lists span years. */
internal fun sparseRows(count: Int, newest: LocalDate = LocalDate.of(2026, 3, 28)): List<TimelineRowState> =
    List(count) { entryRow(newest.minusDays(it * 10L), isLast = it == count - 1) }
