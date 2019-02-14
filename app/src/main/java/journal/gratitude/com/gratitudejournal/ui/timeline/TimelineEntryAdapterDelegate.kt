package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry

class TimelineEntryAdapterDelegate(activity: Activity, private val clickListener: TimelineAdapter.OnClickListener) : AdapterDelegate<List<Entry>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<Entry>, position: Int): Boolean {
        //until there are more view types this will be really simple
        return items[position] is Entry
    }

    override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_timeline_entry, parent, false)
        return TimelineEntryViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: List<Entry>,
        position: Int,
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val isLastItem = position == items.size - 1
        (holder as TimelineEntryViewHolder).bind(items[position], isLastItem)
    }

    inner class TimelineEntryViewHolder(private val binding: ViewDataBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(timelineEntry: Entry, isLastItem: Boolean) {
            binding.setVariable(BR.entryViewModel, TimelineEntryViewModel(timelineEntry, isLastItem, clickListener))
            binding.executePendingBindings()
        }
    }

}