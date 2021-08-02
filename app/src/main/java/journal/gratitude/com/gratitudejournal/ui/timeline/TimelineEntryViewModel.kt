package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toFullString
import journal.gratitude.com.gratitudejournal.util.toStringWithDayOfWeek
import org.threeten.bp.LocalDate

class TimelineEntryViewModel(
        private val timelineItem: Entry,
        private val isLastItem: Boolean,
        private val numEntries: Int,
        private val showDayOfWeek: Boolean,
        val maxLines: Int,
        private val clickListener: OnClickListener
) {

    val content = timelineItem.entryContent

    fun dateString(): String {
        return if (showDayOfWeek) {
            timelineItem.entryDate.toStringWithDayOfWeek()
        } else {
            timelineItem.entryDate.toFullString()
        }
    }

    fun onClick(view: View) {
        clickListener.onClick(view, timelineItem.entryDate, timelineItem.entryContent == "", numEntries)
    }

    fun isCurrentDate(): Boolean {
        return timelineItem.entryDate == LocalDate.now()
    }

    fun isEmptyState(): Int {
        return if (timelineItem.entryContent.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun isTailVisible(): Boolean {
        return isLastItem
    }
}
