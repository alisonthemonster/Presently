package journal.gratitude.com.gratitudejournal.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.presently.presently_local_source.model.Entry
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.ItemMilestoneBinding
import journal.gratitude.com.gratitudejournal.databinding.ItemTimelineEntryBinding
import journal.gratitude.com.gratitudejournal.model.Milestone
import journal.gratitude.com.gratitudejournal.model.TimelineEntry
import journal.gratitude.com.gratitudejournal.model.TimelineItem
import org.threeten.bp.LocalDate

class TimelineAdapter(
    private val showDayOfWeek: Boolean,
    private val linesPerEntry: Int,
    private val onClickListener: OnClickListener
) : ListAdapter<TimelineItem, TimelineAdapter.TimelineViewHolder>(TimelineDiffUtil()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimelineViewHolder {
        return when (viewType) {
            R.layout.item_timeline_entry -> {
                val binding = ItemTimelineEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TimelineViewHolder.EntryViewHolder(binding)
            }
            R.layout.item_milestone -> {
                val binding = ItemMilestoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                TimelineViewHolder.MilestoneViewHolder(binding)
            }
            else -> {
                throw IllegalStateException("Unsupported view type: $viewType")
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TimelineEntry -> R.layout.item_timeline_entry
            is Milestone -> R.layout.item_milestone
        }
    }

    override fun onBindViewHolder(
        holder: TimelineViewHolder,
        position: Int
    ) {
        val item = getItem(position)
        when (holder) {
            is TimelineViewHolder.EntryViewHolder -> {
                val isLastItem = position == (itemCount - 1)
                var numEntries = 0
                for (entry in currentList) {
                    if (entry is TimelineEntry && entry.content.isNotEmpty()) numEntries++
                }
                val viewModel = TimelineEntryViewModel(
                    item as TimelineEntry,
                    isLastItem,
                    numEntries,
                    showDayOfWeek,
                    linesPerEntry,
                    onClickListener
                )
                holder.bind(viewModel)
            }
            is TimelineViewHolder.MilestoneViewHolder -> holder.bind(item as Milestone)
        }
    }

    sealed class TimelineViewHolder(view: View): RecyclerView.ViewHolder(view) {

        class EntryViewHolder(val binding: ItemTimelineEntryBinding) :
            TimelineViewHolder(binding.root) {

            fun bind(viewModel: TimelineEntryViewModel) {

                binding.date.text = viewModel.dateString()
                binding.content.text = viewModel.content
                binding.content.maxLines = viewModel.maxLines
                val hintTextResource =
                    if (viewModel.isCurrentDate()) R.string.what_are_you_thankful_for_today else R.string.what_are_you_thankful_for_yesterday
                binding.emptyState.text = binding.root.context.resources.getText(hintTextResource)
                binding.emptyState.visibility = viewModel.isEmptyState()

                binding.tailCircle.isVisible = viewModel.isTailVisible()
                binding.timelineCircleFilled.isVisible = viewModel.isCurrentDate()
                binding.timelineIcon.isVisible = viewModel.isTailVisible()

                binding.entryContainer.setOnClickListener {
                    viewModel.onClick(it)
                }
            }
        }

        class MilestoneViewHolder(private val binding: ItemMilestoneBinding) :
            TimelineViewHolder(binding.root) {
            fun bind(milestone: Milestone) {
                binding.number.text = milestone.numString
            }
        }
    }

    class TimelineDiffUtil : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(
            oldItem: TimelineItem,
            newItem: TimelineItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: TimelineItem,
            newItem: TimelineItem
        ): Boolean {
            return if (oldItem is TimelineEntry && newItem is TimelineEntry) {
                oldItem.date == newItem.date
            } else if (oldItem is Milestone && newItem is Milestone) {
                oldItem.number == newItem.number
            } else {
                false
            }
        }
    }
}

interface OnClickListener {
    fun onClick(view: View, clickedDate: LocalDate, isNewEntry: Boolean, numEntries: Int)
}