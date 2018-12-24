package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.bindingadapter.Visibility
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate

class TimelineEntryViewModel(
        private val timelineItem: Entry,
        private val isLastItem: Boolean,
        private val clickListener: TimelineAdapter.OnClickListener
) {

    val date = timelineItem.entryDate.toFullString()
    val content = timelineItem.entryContent

    fun onClick(view: View) {
        clickListener.onClick(timelineItem.entryDate)
    }

    @Visibility
    fun isTodayWritten(): Int {
        return if (timelineItem.entryDate == LocalDate.now()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Visibility
    fun isEmptyState(): Int {
        return if (timelineItem.entryContent.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    @Visibility
    fun isTailVisible(): Int {
        return if (isLastItem) View.VISIBLE else View.GONE
    }
}