package journal.gratitude.com.gratitudejournal.ui.search

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH_ITEM
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import org.threeten.bp.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment() {

    private val viewModel: SearchViewModel by viewModels()

    @Inject
    lateinit var analytics: AnalyticsLogger

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val transition =
            TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)

        sharedElementEnterTransition = ChangeBounds().apply {
            sharedElementEnterTransition = transition
            duration = 300
        }

        return ComposeView(requireContext()).apply {
            transitionName = "search_transition"
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme {
                    SearchScreen(
                        viewModel = viewModel,
                        onBackClick = {
                            parentFragmentManager.popBackStack()
                        },
                        onSearchResultClick = { clickedDate ->
                            analytics.recordEvent(CLICKED_SEARCH_ITEM)
                            openEntryScreen(clickedDate)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        analytics.recordView("SearchFragment")

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
}
