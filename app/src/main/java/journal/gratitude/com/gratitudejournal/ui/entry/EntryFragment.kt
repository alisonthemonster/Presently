package journal.gratitude.com.gratitudejournal.ui.entry

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.databinding.EntryFragmentBinding
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import kotlinx.android.synthetic.main.entry_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import android.content.Intent



class EntryFragment : Fragment() {

    private lateinit var viewModel: EntryViewModel
    private lateinit var binding: EntryFragmentBinding
    private val compositeDisposable = CompositeDisposable()


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

        loading_indicator.progress = 1f

        val disposable = RxTextView.afterTextChangeEvents(entry_text)
            .debounce(500, TimeUnit.MILLISECONDS)
            .skip(1)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                loading_indicator.playAnimation()
                viewModel.addNewEntry()
            }

        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
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
