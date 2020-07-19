package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.model.TimelineItem

class TimelineEntryAdapterDelegate(activity: Activity, val showDayOfWeek: Boolean, private val clickListener: TimelineAdapter.OnClickListener) : AdapterDelegate<List<TimelineItem>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<TimelineItem>, position: Int): Boolean {
        return items[position] is Entry
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_timeline_entry, parent, false)
        return TimelineEntryViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: List<TimelineItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val isLastItem = position == items.size - 1
        var numItems = 0
        for (item in items) {
            if (item is Entry && item.entryContent.isNotEmpty()) numItems++
        }
        (holder as TimelineEntryViewHolder).bind(items[position] as Entry, isLastItem, numItems)
    }

    inner class TimelineEntryViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timelineEntry: Entry, isLastItem: Boolean, numEntries: Int) {
            binding.setVariable(BR.entryViewModel, TimelineEntryViewModel(timelineEntry, isLastItem, numEntries, showDayOfWeek, clickListener))
            binding.executePendingBindings()
        }
    }

}