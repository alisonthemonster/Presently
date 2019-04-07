package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import journal.gratitude.com.gratitudejournal.databinding.EntryFragmentBinding
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import kotlinx.android.synthetic.main.entry_fragment.*
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
            EntryViewModelFactory(passedInDate, repository, activity!!.application)
        ).get(EntryViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.entry.observe(this, Observer {
            binding.viewModel = viewModel
        })

        share_button.setOnClickListener {
            val message = viewModel.getShareContent()
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, message)

            startActivity(Intent.createChooser(share, "Share your gratitude"))
        }

        save_button.setOnClickListener {
            viewModel.addNewEntry()
            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(entry_text.windowToken, 0)
            findNavController().navigateUp()
        }
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
