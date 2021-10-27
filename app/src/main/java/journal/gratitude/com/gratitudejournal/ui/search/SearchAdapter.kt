package journal.gratitude.com.gratitudejournal.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import journal.gratitude.com.gratitudejournal.databinding.ItemSearchResultBinding
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toShortMonthString
import java.time.LocalDate

class SearchAdapter(private val onClickListener: OnClickListener) : PagingDataAdapter<Entry, SearchAdapter.SearchEntryViewHolder>(COMPARATOR) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchEntryViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchEntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchEntryViewHolder, position: Int) {
        val item = getItem(position)
        item?.let {
            holder.bind(item)
        }
    }

    inner class SearchEntryViewHolder(private val binding: ItemSearchResultBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: Entry) {
            binding.content.text = entry.entryContent
            binding.year.text = entry.entryDate.year.toString()
            binding.day.text = entry.entryDate.dayOfMonth.toString()
            binding.month.text = entry.entryDate.month.toShortMonthString()
            binding.searchResultContainer.setOnClickListener {
                onClickListener.onClick(entry.entryDate)
            }
        }
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Entry>() {
            override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean =
                    oldItem.entryDate == newItem.entryDate

            override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean =
                    oldItem == newItem
        }
    }

    interface OnClickListener {
        fun onClick(clickedDate: LocalDate)
    }
}