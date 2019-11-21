package journal.gratitude.com.gratitudejournal.ui.search

import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.SearchFragmentBinding
import journal.gratitude.com.gratitudejournal.model.CLICKED_SEARCH_ITEM
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.repository.EntryRepositoryImpl
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineViewModel
import kotlinx.android.synthetic.main.search_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class SearchFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<SearchViewModel> { viewModelFactory }
    private lateinit var binding: SearchFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val disposables = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = SearchFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        val transition = TransitionInflater.from(this.activity).inflateTransition(android.R.transition.move)

        sharedElementEnterTransition = ChangeBounds().apply {
            sharedElementEnterTransition = transition
            duration = 300
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        val obs = RxTextView.textChanges(search_text)
            .debounce(300, TimeUnit.MILLISECONDS)
            .filter { charSequence -> charSequence.isNotBlank() }
            .map<Any> { charSequence -> charSequence.toString() }
            .subscribe {
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.SEARCH_TERM, it as String)
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle)
                search(it)
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
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(search_text.windowToken, 0)

            findNavController().navigateUp()
        }

        val adapter = SearchAdapter(activity!!, object : SearchAdapter.OnClickListener {
            override fun onClick(
                clickedDate: LocalDate
            ) {
                firebaseAnalytics.logEvent(CLICKED_SEARCH_ITEM, null)

                val bundle = bundleOf(EntryFragment.DATE to clickedDate.toString())
                findNavController().navigate(
                        R.id.action_searchFragment_to_entryFragment,
                        bundle
                )
            }
        })
        search_results.layoutManager = LinearLayoutManager(context)
        search_results.adapter = adapter

        viewModel.results.observe(this, Observer {
            binding.viewModel = viewModel
            adapter.submitList(it)
        })

        openKeyboard()
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
