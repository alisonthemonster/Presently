package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment


class ViewPagerAdapter(val fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var itemsList: List<Entry> = emptyList()
    private var numEntries = 0

    fun setItemsListAndEntryCount(list: List<Entry>, entryCount: Int) {
        itemsList = list
        numEntries = entryCount
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    override fun createFragment(position: Int): Fragment {
        val item = itemsList[position]
        return EntryFragment.newInstance(
            date = itemsList[position].entryDate,
            numEntries = numEntries,
            isNewEntry = item.entryContent == "",
            resources = fragment.resources
        )
    }
}

fun ViewPager2.findCurrentFragment(fragmentManager: FragmentManager): Fragment? {
    return fragmentManager.findFragmentByTag("f$currentItem")
}