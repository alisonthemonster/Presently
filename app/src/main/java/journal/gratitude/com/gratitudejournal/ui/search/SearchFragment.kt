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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.TransitionInflater
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.SearchFragmentBinding
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineAdapter
import kotlinx.android.synthetic.main.search_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit


class SearchFragment : Fragment() {

    companion object {
        fun newInstance() = SearchFragment()
    }
    private lateinit var binding: SearchFragmentBinding
    private lateinit var viewModel: SearchViewModel

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val entryDao = EntryDatabase.getDatabase(activity!!.application).entryDao()
        val repository = EntryRepository(entryDao) //TODO look into sharing across all fragments

        viewModel = ViewModelProviders.of(this, SearchViewModelFactory(repository)).get(SearchViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        binding = SearchFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        val transition = TransitionInflater.from(this.activity).inflateTransition(android.R.transition.move)

        sharedElementEnterTransition = ChangeBounds().apply {
            sharedElementEnterTransition = transition
            duration = 200
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val obs = RxTextView.textChanges(search_text)
            .debounce(300, TimeUnit.MILLISECONDS)
            .map<Any> { charSequence -> charSequence.toString() }
            .subscribe {
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
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(search_text.windowToken, 0)

            findNavController().navigateUp()
        }

        val adapter = SearchAdapter(activity!!, object : TimelineAdapter.OnClickListener {
            override fun onClick(clickedDate: LocalDate) {

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

    private fun dismissKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(search_text.windowToken, 0)
    }


}
