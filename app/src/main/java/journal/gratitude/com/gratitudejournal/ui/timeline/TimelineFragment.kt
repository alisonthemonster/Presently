package journal.gratitude.com.gratitudejournal.ui.timeline

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.TimelineFragmentBinding
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import kotlinx.android.synthetic.main.timeline_fragment.*
import org.threeten.bp.LocalDate


class TimelineFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = TimelineFragment()
    }

    private lateinit var viewModel: TimelineViewModel
    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: TimelineFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val entryDao = EntryDatabase.getDatabase(activity!!.application).entryDao()
        val repository = EntryRepository(entryDao) //TODO look into sharing across both fragments

        viewModel = ViewModelProviders.of(this, TimelineViewModelFactory(repository)).get(TimelineViewModel::class.java)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = TimelineFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeline_recycler_view.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(context)
        adapter = TimelineAdapter(activity!!, object : TimelineAdapter.OnClickListener {
            override fun onClick(clickedDate: LocalDate) {
                fragmentManager!!
                    .beginTransaction()
                    .replace(R.id.content_frame, EntryFragment.newInstance(clickedDate), "Blerg")
                    .commitAllowingStateLoss()
            }

        })
        timeline_recycler_view.adapter = adapter

        viewModel.entries.observe(this, Observer {
            binding.viewModel = viewModel
        })

    }

}
