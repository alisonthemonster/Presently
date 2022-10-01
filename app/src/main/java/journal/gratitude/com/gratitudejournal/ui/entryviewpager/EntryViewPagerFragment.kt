package journal.gratitude.com.gratitudejournal.ui.entryviewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.*
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.asMavericksArgs
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.databinding.EntryViewPagerFragmentBinding
import journal.gratitude.com.gratitudejournal.ui.entry.EntryScreenCallbacks
import org.threeten.bp.LocalDate

@AndroidEntryPoint
class EntryViewPagerFragment : Fragment(), MavericksView {

    private var _binding: EntryViewPagerFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewPagerViewModel by fragmentViewModel()

    lateinit var adapter: ViewPagerAdapter

    var previousPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                parentFragmentManager.popBackStack()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = EntryViewPagerFragmentBinding.inflate(inflater, container, false)
        adapter = ViewPagerAdapter(this)
        binding.viewPager.adapter = adapter
        checkForEditsBeforeSwipe()
        return binding.root
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (state.entriesList.isNotEmpty()) {
                adapter.setItemsListAndEntryCount(state.entriesList, state.numEntries)
                val selectedItem = state.entriesList.find { it.entryDate == state.selectedDate }
                binding.viewPager.setCurrentItem((state.entriesList.indexOf(selectedItem)), false)
            }
        }
    }

    private fun checkForEditsBeforeSwipe() {
        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
                when (state) {
                    SCROLL_STATE_DRAGGING -> {
                        val canSwipe =
                            binding.viewPager.findCurrentFragment(childFragmentManager) as EntryScreenCallbacks

                        //check if there were any edits ?
                        if (canSwipe.anyEditsMade()) {
                            binding.viewPager.isUserInputEnabled = false

                            canSwipe.hideKeyboard()
                            canSwipe.showSaveDialog()
                            canSwipe.setParentCallback {
                                binding.viewPager.isUserInputEnabled = true
                            }

                            //as Page might be half Scrolled, we want to reset the position to current page so that page is fully visible
                            binding.viewPager.setCurrentItem(previousPosition, false)
                        } else {
                            binding.viewPager.isUserInputEnabled = true
                        }

                    }
                }
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                previousPosition = position
            }
        })

    }

    companion object {
        fun newInstance(
            date: LocalDate,
        ): EntryViewPagerFragment {

            val fragment = EntryViewPagerFragment()
            fragment.arguments = EntryViewPagerArgs(
                date
            ).asMavericksArgs()
            return fragment
        }

    }
}

