package journal.gratitude.com.gratitudejournal.ui.themes

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import journal.gratitude.com.gratitudejournal.databinding.ItemThemeBinding
import journal.gratitude.com.gratitudejournal.model.Theme

class ThemeListAdapter(private val listener: ThemeFragment.OnThemeSelectedListener?) :
    ListAdapter<Theme, ThemeListAdapter.ThemeViewHolder>(ThemeDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ThemeViewHolder(
        ItemThemeBinding.inflate(LayoutInflater.from(parent.context), parent, false), listener
    )

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) = holder.bind(getItem(position))

    fun addData(data: List<Theme>) {
        submitList(data.toMutableList())
    }

    inner class ThemeViewHolder(
        private val binding: ItemThemeBinding,
        private val listener: ThemeFragment.OnThemeSelectedListener?
    ) : RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
            listener?.onThemeSelected(clicked.name)
        }

        fun bind(item: Theme) = with(binding) {
            backgroundView.setBackgroundColor(item.backgroundColor)
            header.setBackgroundColor(item.headerColor)
            logo.setTextColor(item.headerItemColor)
            icon.setImageResource(item.icon)
            if (!item.multicolorIcon) icon.setColorFilter(item.iconColor) else icon.clearColorFilter()
            timelineLine.setBackgroundColor(item.headerColor)
            themeName.text = item.name
            themeName.setTextColor(item.iconColor)
        }
    }

    private class ThemeDC : DiffUtil.ItemCallback<Theme>() {
        override fun areItemsTheSame(
            oldItem: Theme,
            newItem: Theme
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: Theme,
            newItem: Theme
        ): Boolean {
            return oldItem.name == newItem.name
        }
    }
}