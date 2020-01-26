package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineItem


class TimelineMilstoneAdapterDelegate(activity: Activity) : AdapterDelegate<List<TimelineItem>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<TimelineItem>, position: Int): Boolean {
        return items[position] is Milestone
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, journal.gratitude.com.gratitudejournal.R.layout.item_milestone, parent, false)
        return MilestoneViewHolder(binding)
    }

    override fun onBindViewHolder(
        items: List<TimelineItem>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {

        (holder as MilestoneViewHolder).bind(items[position] as Milestone)
    }


    inner class MilestoneViewHolder(private val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(milestone: Milestone) {
            binding.setVariable(BR.milestone, milestone)
            binding.executePendingBindings()
        }
    }
}