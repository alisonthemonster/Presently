package journal.gratitude.com.gratitudejournal.ui.search

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.presently.logging.AnalyticsLogger
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.SearchFragmentBinding
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH_ITEM
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.util.textChanges
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()
    @Inject lateinit var analytics: AnalyticsLogger

    private var _binding: SearchFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = SearchFragmentBinding.inflate(inflater, container, false)

        val transition =
            TransitionInflater.from(this.activity).inflateTransition(android.R.transition.move)

        sharedElementEnterTransition = ChangeBounds().apply {
            sharedElementEnterTransition = transition
            duration = 300
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics.recordView("SearchFragment")

        val adapter = SearchAdapter(object : SearchAdapter.OnClickListener {
            override fun onClick(
                clickedDate: LocalDate
            ) {
                analytics.recordEvent(CLICKED_SEARCH_ITEM)
                openEntryScreen(clickedDate)
            }
        })

        lifecycleScope.launch {
            adapter.loadStateFlow.collect {
                val refresher = it.refresh
                val displayEmptyMessage =
                    (refresher is LoadState.NotLoading && adapter.itemCount == 0)

                binding.searchResults.isVisible = !displayEmptyMessage
                binding.noResults.isVisible = displayEmptyMessage
                binding.noResultsIcon.isVisible = displayEmptyMessage
                // Handle icon display issues in older versions
                if(Build.VERSION.SDK_INT <= 23)
                    binding.noResultsIcon.imageTintList = context?.getColorStateList(R.color.text_color)
                binding.noResults.isVisible = displayEmptyMessage
            }
        }

        binding.searchResults.layoutManager = LinearLayoutManager(context)
        binding.searchResults.adapter = adapter

        lifecycleScope.launch {
            binding.searchText.textChanges()
                .debounce(300)
                .filter { charSequence -> !charSequence.isNullOrBlank() }
                .map { charSequence -> charSequence.toString() }
                .flatMapLatest { query ->
                    viewModel.search(query)
                }
                .collectLatest {results ->
                    adapter.submitData(results)
                }
        }

        binding.searchText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> { dismissKeyboard(); true }
                else -> false
            }
        }

        binding.searchIcon.setOnClickListener {
            dismissKeyboard()
        }

        binding.backIcon.setOnClickListener {
            dismissKeyboard()
            parentFragmentManager.popBackStack()
        }

        openKeyboard()

        ViewCompat.setOnApplyWindowInsetsListener(binding.searchContainer) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
        window.statusBarColor = typedValue.data
    }

    private fun openEntryScreen(clickedDate: LocalDate) {
        val fragment = EntryFragment.newInstance(clickedDate, null, false, resources)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TimelineFragment.TIMELINE_TO_ENTRY)
            .commit()
    }

    private fun openKeyboard() {
        binding.searchText.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(binding.searchText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun dismissKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.searchText.windowToken, 0)
    }

}
