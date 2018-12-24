package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.View
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toFullString

class TimelineEntryViewModel(timelineItem: Entry) {

    val date = timelineItem.entryDate.toFullString()
    val content = timelineItem.entryContent

    fun onClick(view: View) {
        //navigate
    }
}