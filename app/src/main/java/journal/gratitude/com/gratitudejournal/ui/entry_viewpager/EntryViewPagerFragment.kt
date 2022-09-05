package journal.gratitude.com.gratitudejournal.ui.entry_viewpager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.asMavericksArgs
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.databinding.EntryViewPagerFragmentBinding
import org.threeten.bp.LocalDate

@AndroidEntryPoint
class EntryViewPagerFragment : Fragment(), MavericksView {

    private var _binding: EntryViewPagerFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewPagerViewModel by fragmentViewModel()

    lateinit var adapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                withState(viewModel) {
                    parentFragmentManager.popBackStack()
                }
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
        return binding.root
    }

    override fun invalidate() {
        withState(viewModel) { state ->
            if (state.entriesList.isNotEmpty()) {
                adapter.setItemsList(state.entriesList)
                val selectedItem = state.entriesList.find { it.entryDate == state.selectedDate }
                binding.viewPager.setCurrentItem((state.entriesList.indexOf(selectedItem)), false)
            }
        }
    }

    companion object {
        fun newInstance(
            date: LocalDate,
            numEntries: Int?,
            isNewEntry: Boolean,
        ): EntryViewPagerFragment {

            val fragment = EntryViewPagerFragment()
            fragment.arguments = EntryViewPagerArgs(
                date,
                numEntries,
                isNewEntry,
            ).asMavericksArgs()
            return fragment
        }

    }

}