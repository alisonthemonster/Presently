package journal.gratitude.com.gratitudejournal.ui.entry

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.EntryFragmentBinding
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import kotlinx.android.synthetic.main.entry_fragment.*
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate

class EntryFragment : Fragment() {

    private lateinit var viewModel: EntryViewModel
    private lateinit var binding: EntryFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = EntryFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val passedInDate = arguments?.getString(DATE) ?: LocalDate.now().toString()

        val entryDao = EntryDatabase.getDatabase(activity!!.application).entryDao()

        val repository = EntryRepository(entryDao)

        viewModel = ViewModelProviders.of(
            this,
            EntryViewModelFactory(passedInDate, repository)
        ).get(EntryViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.entry.observe(this, Observer {
            binding.viewModel = viewModel
        })

        timeline_button.setOnClickListener {
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.fragment_container, TimelineFragment.newInstance(), "Blerg")
                .commitAllowingStateLoss()
        }

        // when text changes
            // entryViewModel.addEntry(entry)
    }

    companion object {
        const val DATE = "date_key"

        fun newInstance(date: LocalDate = LocalDate.now()): EntryFragment {
            val fragment = EntryFragment()

            val bundle = Bundle()
            bundle.putString(DATE, date.toString())
            fragment.arguments = bundle

            return fragment
        }

    }
}
