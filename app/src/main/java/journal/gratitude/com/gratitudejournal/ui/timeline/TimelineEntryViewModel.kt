package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import com.presently.date_utils.toFullString
import com.presently.date_utils.toStringWithDayOfWeek
import journal.gratitude.com.gratitudejournal.model.TimelineEntry
import org.threeten.bp.LocalDate

class TimelineEntryViewModel(
    private val timelineItem: TimelineEntry,
    private val isLastItem: Boolean,
    private val numEntries: Int,
    private val showDayOfWeek: Boolean,
    val maxLines: Int,
    private val clickListener: OnClickListener
) {

    val content = timelineItem.content

    fun dateString(): String {
        return if (showDayOfWeek) {
            timelineItem.date.toStringWithDayOfWeek()
        } else {
            timelineItem.date.toFullString()
        }
    }

    fun onClick(view: View) {
        clickListener.onClick(view, timelineItem.date, timelineItem.content == "", numEntries)
    }

    fun isCurrentDate(): Boolean {
        return timelineItem.date == LocalDate.now()
    }

    fun isEmptyState(): Int {
        return if (timelineItem.content.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun isTailVisible(): Boolean {
        return isLastItem
    }
}
