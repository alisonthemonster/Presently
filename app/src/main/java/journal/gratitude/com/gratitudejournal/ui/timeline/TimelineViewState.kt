package journal.gratitude.com.gratitudejournal.ui.timeline

import journal.gratitude.com.gratitudejournal.model.TimelineItem
import org.threeten.bp.LocalDate

data class TimelineViewState(
    val timelineItems: List<TimelineItem> = emptyList(),
    val datesWritten: Set<LocalDate> = emptySet()
) {
    val numEntries = datesWritten.size
}