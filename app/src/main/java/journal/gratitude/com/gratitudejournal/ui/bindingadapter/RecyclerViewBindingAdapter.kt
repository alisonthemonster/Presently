package journal.gratitude.com.gratitudejournal.ui.bindingadapter

import androidx.databinding.BindingAdapter

@BindingAdapter("data")
fun <T> setRecyclerViewData(recyclerView: androidx.recyclerview.widget.RecyclerView, data: T) {
    if (recyclerView.adapter is BindableAdapter<*>) {
        (recyclerView.adapter as BindableAdapter<T>).setData(data)
    }
}