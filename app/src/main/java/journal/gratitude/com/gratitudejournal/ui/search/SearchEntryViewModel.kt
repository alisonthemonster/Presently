package journal.gratitude.com.gratitudejournal.ui.search

import android.view.View
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter
import org.threeten.bp.format.TextStyle
import java.util.*

class SearchEntryViewModel(
        private val entry: Entry,
        private val clickListener: SearchAdapter.OnClickListener
) {

    val content = entry.entryContent

    fun getDay(): String {
        return entry.entryDate.dayOfMonth.toString()
    }

    fun getYear(): String  {
        return entry.entryDate.year.toString()
    }

    fun getMonth(): String {
        return entry.entryDate.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

    fun onClick(view: View) {
        clickListener.onClick(entry.entryDate)
    }

}