package journal.gratitude.com.gratitudejournal.ui.search

import android.view.View
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter
import journal.gratitude.com.gratitudejournal.util.toFullString

class SearchEntryViewModel(
        private val entry: Entry,
        private val clickListener: TimelineAdapter.OnClickListener
) {

    val date = entry.entryDate.toFullString()
    val content = entry.entryContent

    fun onClick(view: View) {
        clickListener.onClick(entry.entryDate)
    }

}