package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.jakewharton.rxbinding2.widget.RxTextView
import dagger.android.support.DaggerFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.EntryFragmentBinding
import journal.gratitude.com.gratitudejournal.model.CLICKED_PROMPT
import journal.gratitude.com.gratitudejournal.model.COPIED_QUOTE
import journal.gratitude.com.gratitudejournal.model.EDITED_EXISTING_ENTRY
import journal.gratitude.com.gratitudejournal.model.Milestone.Companion.milestones
import journal.gratitude.com.gratitudejournal.model.SHARED_ENTRY
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.BACKUP_CADENCE
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import kotlinx.android.synthetic.main.entry_fragment.*
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class EntryFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<EntryViewModel> { viewModelFactory }
    private lateinit var binding: EntryFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

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
        viewModel.setDate(passedInDate)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val isEdited = viewModel.hasUserEdits.get()
                val isEmpty = viewModel.isEmpty.get()
                if (isEdited && !isEmpty) {
                    showUnsavedEntryDialog()
                } else {
                    findNavController().navigateUp()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        viewModel.entry.observe(viewLifecycleOwner, Observer {
            binding.viewModel = viewModel
        })

        prompt_button.setOnClickListener {
            firebaseAnalytics.logEvent(CLICKED_PROMPT, null)
            viewModel.getRandomPromptHintString()
            val v = it as ImageView
            val d = v.drawable as Animatable?
            d?.start()
            binding.viewModel = viewModel
        }

        share_button.setOnClickListener {
            firebaseAnalytics.logEvent(SHARED_ENTRY, null)

            val message = viewModel.getShareContent()
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, message)

            startActivity(Intent.createChooser(share, getString(R.string.share_progress)))
        }

        save_button.setOnClickListener {
            val numEntries = arguments?.getInt(NUM_ENTRIES) ?: 0
            val isNewEntry = arguments?.getBoolean(IS_NEW_ENTRY) ?: false
            if (isNewEntry) {
                val bundle = Bundle()
                bundle.putInt(FirebaseAnalytics.Param.LEVEL, (numEntries + 1))

                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
                if (milestones.contains(numEntries + 1)) {
                    CelebrateDialogFragment.newInstance(numEntries + 1)
                        .show(fragmentManager!!, "CelebrateDialogFragment")
                }
            } else {
                firebaseAnalytics.logEvent(EDITED_EXISTING_ENTRY, null)
            }

            viewModel.addNewEntry()
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
            imm?.hideSoftInputFromWindow(entry_text.windowToken, 0)

            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
            val accessToken = sharedPrefs.getString("access-token", null)
            val cadence = sharedPrefs.getString(BACKUP_CADENCE, "0") ?: "0"
            if (accessToken != null && cadence == "2") {
                val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                    .addTag(DropboxUploader.PRESENTLY_BACKUP)
                    .build()
                WorkManager.getInstance(context!!).enqueue(uploadWorkRequest)
            }

            findNavController().navigateUp()
        }

        inspiration.setOnLongClickListener {
            val quote = viewModel.getInspirationString()
            val clipboard =
                getSystemService<ClipboardManager>(context!!, ClipboardManager::class.java)
            clipboard?.primaryClip = ClipData.newPlainText("Gratitude quote", quote)
            firebaseAnalytics.logEvent(COPIED_QUOTE, null)
            Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            true
        }

        val disposable = RxTextView.afterTextChangeEvents(entry_text)
            .debounce(500, TimeUnit.MILLISECONDS)
            .skip(1) //skip data binding
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                viewModel.userEdited()
            }

        compositeDisposable.add(disposable)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    private fun showUnsavedEntryDialog() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.are_you_sure)
                setMessage(R.string.unsaved_text)
                setPositiveButton(R.string.continue_to_exit) { dialog, id ->
                    findNavController().navigateUp()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    companion object {
        const val DATE = "date_key"
        const val IS_NEW_ENTRY = "is_new_entry"
        const val NUM_ENTRIES = "num_entries"

        fun newInstance(date: LocalDate = LocalDate.now()): EntryFragment {
            val fragment = EntryFragment()

            val bundle = Bundle()
            bundle.putString(DATE, date.toString())
            fragment.arguments = bundle

            return fragment
        }

    }
}
