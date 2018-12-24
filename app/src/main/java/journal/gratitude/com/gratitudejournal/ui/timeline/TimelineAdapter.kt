package journal.gratitude.com.gratitudejournal.ui.timeline

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.bindingadapter.BindableAdapter
import org.threeten.bp.LocalDate

class TimelineAdapter(activity: Activity, onClickListener: OnClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), BindableAdapter<List<Entry>> {

    private lateinit var entries: List<Entry>

    private val delegatesManager = AdapterDelegatesManager<List<Entry>>()

    init {
        delegatesManager.addDelegate(TimelineEntryAdapterDelegate(activity, onClickListener))
    }

    override fun setData(data: List<Entry>) {
        entries = data

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegatesManager.onCreateViewHolder(viewGroup, viewType)
    }

    override fun getItemCount(): Int {
        return entries.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return delegatesManager.onBindViewHolder(entries, position, holder)
    }

    override fun getItemViewType(position: Int): Int {
        return delegatesManager.getItemViewType(entries, position)
    }

    interface OnClickListener {
        fun onClick(clickedDate: LocalDate)
    }
}