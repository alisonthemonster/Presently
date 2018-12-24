package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.model.Entry
import org.threeten.bp.LocalDate

class EmptyTimelineEntryAdapterDelegate(
    activity: Activity,
    private val clickListener: TimelineAdapter.OnClickListener
) : AdapterDelegate<List<Entry>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<Entry>, position: Int): Boolean {
        return items[position].entryDate == LocalDate.now() && items[position].entryContent.isEmpty()
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding =
            DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_timeline_entry_empty, parent, false)
        return EmptyTimelineEntryViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: List<Entry>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        val isLastItem = position == items.size - 1
        (holder as EmptyTimelineEntryViewHolder).bind(items[position], isLastItem)
    }

    inner class EmptyTimelineEntryViewHolder(private val binding: ViewDataBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(timelineEntry: Entry, isLastItem: Boolean) {

            binding.setVariable(BR.viewModel, TimelineEntryViewModel(timelineEntry, isLastItem, clickListener))
            binding.executePendingBindings()
        }

    }

}