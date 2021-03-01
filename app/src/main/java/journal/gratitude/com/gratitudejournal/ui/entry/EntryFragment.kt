package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.mvrx.MavericksView
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
import journal.gratitude.com.gratitudejournal.util.setStatusBarColorsForBackground
import kotlinx.android.synthetic.main.entry_fragment.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class EntryFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<EntryViewModel> { viewModelFactory }
    private lateinit var binding: EntryFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private val compositeDisposable = CompositeDisposable()

    val args: EntryFragmentArgs by navArgs()

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

        val passedInDate = args.date
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

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        viewModel.entry.observe(viewLifecycleOwner, Observer {
            binding.viewModel = viewModel
        })

        ViewCompat.setOnApplyWindowInsetsListener(entry) { v, insets ->
            val sysWindow =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = sysWindow.bottom, top = sysWindow.top)
            insets
        }

        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.backgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)

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
            val numEntries = args.numEntries
            val isNewEntry = args.isNewEntry
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
                WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
            }

            findNavController().navigateUp()
        }

        inspiration.setOnLongClickListener {
            val quote = viewModel.getInspirationString()
            val clipboard =
                getSystemService<ClipboardManager>(requireContext(), ClipboardManager::class.java)
            clipboard?.setPrimaryClip(ClipData.newPlainText("Gratitude quote", quote))
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
}
