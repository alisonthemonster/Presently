package journal.gratitude.com.gratitudejournal.ui.search

import android.app.Activity
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter

class SearchAdapter(activity: Activity, onClickListener: TimelineAdapter.OnClickListener) : PagedListAdapter<Entry, RecyclerView.ViewHolder>(COMPARATOR) {

    private val delegatesManager = AdapterDelegatesManager<List<Entry>>()

    init {
        delegatesManager.addDelegate(SearchEntryAdapterDelegate(activity, onClickListener))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegatesManager.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        val list = if (item == null) emptyList() else listOf(item)
        return delegatesManager.onBindViewHolder(list, 0, holder)
    }

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Entry>() {
            override fun areItemsTheSame(oldItem: Entry, newItem: Entry): Boolean =
                    oldItem.entryDate == newItem.entryDate

            override fun areContentsTheSame(oldItem: Entry, newItem: Entry): Boolean =
                    oldItem == newItem
        }
    }
}