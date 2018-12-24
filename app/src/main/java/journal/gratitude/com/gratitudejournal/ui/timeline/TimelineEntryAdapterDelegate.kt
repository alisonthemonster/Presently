package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry

class TimelineEntryAdapterDelegate(activity: Activity) : AdapterDelegate<List<Entry>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<Entry>, position: Int): Boolean {
        return items[position].entryContent.isNotEmpty()
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_timeline_entry, parent, false)
        return TimelineEntryViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: List<Entry>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        (holder as TimelineEntryViewHolder).bind(items[position])
    }

    inner class TimelineEntryViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timelineEntry: Entry) {
            binding.setVariable(BR.entryViewModel, TimelineEntryViewModel(timelineEntry))
            binding.executePendingBindings()
        }
    }

}