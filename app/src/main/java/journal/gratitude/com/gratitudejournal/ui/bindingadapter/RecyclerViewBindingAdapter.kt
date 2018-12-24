package journal.gratitude.com.gratitudejournal.ui.bindingadapter

import android.databinding.BindingAdapter
import android.support.v7.widget.RecyclerView

@BindingAdapter("data")
fun <T> setRecyclerViewData(recyclerView: RecyclerView, data: T) {
    if (recyclerView.adapter is BindableAdapter<*>) {
        (recyclerView.adapter as BindableAdapter<T>).setData(data)
    }
}