package journal.gratitude.com.gratitudejournal.ui.search

import android.app.Activity
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import journal.gratitude.com.gratitudejournal.BR
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.Entry

class SearchEntryAdapterDelegate(activity: Activity, private val clickListener: SearchAdapter.OnClickListener) : AdapterDelegate<List<Entry>>() {

    private val inflater = activity.layoutInflater

    override fun isForViewType(items: List<Entry>, position: Int): Boolean {
        //until there are more view types this will be really simple
        return items[position] is Entry
    }

    override fun onCreateViewHolder(parent: ViewGroup): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(inflater, R.layout.item_search_result, parent, false)
        return SearchEntryViewHolder(binding)
    }

    override fun onBindViewHolder(
            items: List<Entry>,
            position: Int,
            holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
            payloads: MutableList<Any>
    ) {
        (holder as SearchEntryViewHolder).bind(items[position])
    }

    inner class SearchEntryViewHolder(private val binding: ViewDataBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: Entry) {
            binding.setVariable(BR.searchEntryViewModel, SearchEntryViewModel(entry, clickListener))
            binding.executePendingBindings()
        }
    }

}