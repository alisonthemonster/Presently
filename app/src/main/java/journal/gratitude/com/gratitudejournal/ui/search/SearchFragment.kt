package journal.gratitude.com.gratitudejournal.ui.search

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.jakewharton.rxbinding2.widget.RxTextView
import com.presently.analytics.PresentlyAnalytics
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.SearchFragmentBinding
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH_ITEM
import journal.gratitude.com.gratitudejournal.model.SEARCHING
import journal.gratitude.com.gratitudejournal.model.SEARCH_SCREEN
import journal.gratitude.com.gratitudejournal.model.TIMELINE_SCREEN
import journal.gratitude.com.gratitudejournal.util.setStatusBarColorsForBackground
import kotlinx.android.synthetic.main.search_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analytics: PresentlyAnalytics

    private val viewModel by viewModels<SearchViewModel> { viewModelFactory }
    private lateinit var binding: SearchFragmentBinding

    private val disposables = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = SearchFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

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

        val obs = RxTextView.textChanges(search_text)
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { charSequence -> charSequence.isNotBlank() }
            .map<Any> { charSequence -> charSequence.toString() }
            .subscribe {
                analytics.recordEvent(SEARCHING)
                search(it as String)
            }
        disposables.add(obs)

        search_text.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    dismissKeyboard()
                    return true
                }
                return false
            }
        })

        search_icon.setOnClickListener {
            dismissKeyboard()
        }

        back_icon.setOnClickListener {
            dismissKeyboard()
            findNavController().navigateUp()
        }

        val adapter = SearchAdapter(requireActivity(), object : SearchAdapter.OnClickListener {
            override fun onClick(
                clickedDate: LocalDate
            ) {
                analytics.recordEvent(CLICKED_SEARCH_ITEM)

                val navController = findNavController()
                if (navController.currentDestination?.id == R.id.searchFragment) {
                    val directions =
                        SearchFragmentDirections.actionSearchFragmentToEntryFragment(clickedDate.toString())
                    navController.navigate(directions)
                }
            }
        })
        search_results.layoutManager = LinearLayoutManager(context)
        search_results.adapter = adapter

        viewModel.results.observe(viewLifecycleOwner, Observer {
            binding.viewModel = viewModel
            adapter.submitList(it)
        })

        openKeyboard()

        ViewCompat.setOnApplyWindowInsetsListener(container) { v, insets ->
            v.updatePadding(top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top)
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
        window.statusBarColor = typedValue.data

    }

    override fun onStart() {
        super.onStart()
        analytics.recordView(SEARCH_SCREEN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposables.clear() // Using clear will clear all, but can accept new disposable
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.dispose() // Using dispose will clear all and set isDisposed = true, so it will not accept any new disposable
    }

    private fun search(query: String) {
        if (query.isNotEmpty()) {
            viewModel.search(query)
        }
    }

    private fun openKeyboard() {
        search_text.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(search_text, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun dismissKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(search_text.windowToken, 0)
    }


}
