package journal.gratitude.com.gratitudejournal.ui.entry_viewpager

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment


class ViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var itemsList: List<Entry> = emptyList()

    fun setItemsList(list: List<Entry>) {
        itemsList = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = itemsList[position]
        var numEntries = 0
        for (entry in itemsList) {
            if (entry is Entry && entry.entryContent.isNotEmpty()) numEntries++
        }

        return EntryFragment.newInstance(
            date = itemsList[position].entryDate,
            numEntries = 0,
            isNewEntry = item.entryContent == "",
            resources = fragment.resources
        )
    }
}