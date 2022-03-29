package journal.gratitude.com.gratitudejournal.ui.timeline

import journal.gratitude.com.gratitudejournal.model.TimelineItem
import org.threeten.bp.LocalDate

data class TimelineViewState(
    val entries: List<TimelineItem> = emptyList(),
    val datesWritten: List<LocalDate> = emptyList()
) {
    val numEntries = datesWritten.size
}