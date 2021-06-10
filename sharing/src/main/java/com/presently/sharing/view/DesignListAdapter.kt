package com.presently.sharing.view

import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.presently.sharing.R
import com.presently.sharing.data.SharingViewDesign
import com.presently.sharing.databinding.ItemDesignBinding

class DesignListAdapter(private val listener: SharingFragment.OnDesignSelectedListener?) :
    ListAdapter<SharingViewDesign, DesignListAdapter.ThemeViewHolder>(DesignDC()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ThemeViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_design, parent, false), listener
    )

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) =
        holder.bind(getItem(position))

    fun addData(data: List<SharingViewDesign>) {
        submitList(data.toMutableList())
    }

    inner class ThemeViewHolder(
        itemView: View,
        private val listener: SharingFragment.OnDesignSelectedListener?
    ) : RecyclerView.ViewHolder(itemView), OnClickListener {

        val binding = ItemDesignBinding.bind(itemView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View?) {

            if (adapterPosition == RecyclerView.NO_POSITION) return

            val clicked = getItem(adapterPosition)
            listener?.onDesignSelected(clicked)
        }

        fun bind(item: SharingViewDesign) = with(itemView) {
            binding.designBackground.setBackgroundColor(ContextCompat.getColor(context,item.backgroundColor))
            binding.presentlyLogo.setColorFilter(
                ContextCompat.getColor(context, item.headerTextColor),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }
    }


    private class DesignDC : DiffUtil.ItemCallback<SharingViewDesign>() {
        override fun areItemsTheSame(
            oldItem: SharingViewDesign,
            newItem: SharingViewDesign
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: SharingViewDesign,
            newItem: SharingViewDesign
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
}