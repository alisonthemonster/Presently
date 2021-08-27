package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.*
import android.content.res.Resources
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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.asMavericksArgs
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.jakewharton.rxbinding2.widget.RxTextView
import com.presently.date_utils.toFullString
import com.presently.logging.AnalyticsLogger
import com.presently.settings.BackupCadence
import com.presently.settings.PresentlySettings
import com.presently.sharing.view.SharingFragment
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import journal.gratitude.com.gratitudejournal.databinding.EntryFragmentBinding
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.LocalDate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class EntryFragment : Fragment(), MavericksView {

    private val viewModel: EntryViewModel by fragmentViewModel()
    @Inject
    lateinit var settings: PresentlySettings
    @Inject
    lateinit var analytics: AnalyticsLogger

    private var _binding: EntryFragmentBinding? = null
    private val binding get() = _binding!!

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.onCreate()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                withState(viewModel, {
                    val isEdited = it.hasUserEdits
                    val isEmpty = it.isEmpty
                    if (isEdited && !isEmpty) {
                        showUnsavedEntryDialog()
                    } else {
                        parentFragmentManager.popBackStack()
                    }
                })
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EntryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        prepareWindow()

        with(binding) {

            promptButton.setOnClickListener {
                val v = it as ImageView
                val d = v.drawable as Animatable?
                d?.start()

                viewModel.changePrompt()
            }

            shareButton.setOnClickListener {
                analytics.recordEvent(SHARED_ENTRY)
                withState(viewModel, {
                    openSharingScreen(it.entryContent, date.text.toString())
                })
            }

            saveButton.setOnClickListener {
                viewModel.saveEntry()
            }

            val showQuote = settings.shouldShowQuote()
            if (!showQuote) {
                inspiration.visibility = View.GONE
            }

            inspiration.setOnLongClickListener {
                withState(viewModel, {
                    val quote = it.quote
                    val clipboard =
                        ContextCompat.getSystemService<ClipboardManager>(
                            requireContext(),
                            ClipboardManager::class.java
                        )
                    clipboard?.setPrimaryClip(ClipData.newPlainText("Gratitude quote", quote))
                    analytics.recordEvent(COPIED_QUOTE)
                    Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
                })
                true
            }

            val disposable = RxTextView.afterTextChangeEvents(entryText)
                .debounce(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.onTextChanged(it.editable().toString())
                }
            compositeDisposable.add(disposable)
        }
    }

    private fun openSharingScreen(entryContent: String, entryDate: String) {
        val fragment = SharingFragment.newInstance(entryDate, entryContent)
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(ENTRY_TO_SHARE)
            .commit()
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.date) {
            LocalDate.now() -> {
                binding.date.text = resources.getString(R.string.today)
                binding.thankfulFor.text = resources.getString(R.string.iam)
            }
            LocalDate.now().minusDays(1) -> {
                binding.date.text = resources.getString(R.string.yesterday)
                binding.thankfulFor.text = resources.getString(R.string.iwas)
            }
            else -> {
                binding.date.text = state.date.toFullString()
                binding.thankfulFor.text = resources.getString(R.string.iwas)
            }
        }
        binding.inspiration.text = state.quote
        binding.entryText.hint = state.hint
        setEditText(state.entryContent)
        binding.shareButton.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
        binding.promptButton.visibility = if (!state.isEmpty) View.GONE else View.VISIBLE
        if (state.isSaved) {
            onEntrySaved()
        }
        if (state.milestoneNumber != 0) {
            onEntrySaved()
            CelebrateDialogFragment.newInstance(state.milestoneNumber)
                .show(parentFragmentManager, "CelebrateDialogFragment")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!compositeDisposable.isDisposed) {
            compositeDisposable.dispose()
        }
    }

    private fun setEditText(newText: String) {
        val oldText = binding.entryText.text.toString()
        if (newText != oldText && newText.isNotEmpty()) {
            binding.entryText.setText(newText)
            binding.entryText.setSelection(newText.length)
        }
    }

    private fun prepareWindow() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.entry) { v, insets ->
            val sysWindow =
                insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.updatePadding(bottom = sysWindow.bottom, top = sysWindow.top)
            insets
        }

        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.timelineBackgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    private fun hideKeyboard() {
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(binding.entryText.windowToken, 0)
    }

    private fun backupEntryIfNeeded() {
        val dbxCredential = settings.getAccessToken()

        val cadence = settings.getAutomaticBackupCadence()
        if (dbxCredential != null && cadence == BackupCadence.EVERY_CHANGE) {
            val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                .addTag(DropboxUploader.PRESENTLY_BACKUP)
                .build()
            WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
        }
    }

    private fun onEntrySaved() {
        hideKeyboard()
        backupEntryIfNeeded()
        parentFragmentManager.popBackStack()
    }

    private fun showUnsavedEntryDialog() {
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.are_you_sure)
                setMessage(R.string.unsaved_text)
                setPositiveButton(R.string.continue_to_exit) { _, _ ->
                    parentFragmentManager.popBackStack()
                }
                setNegativeButton(R.string.cancel) { _, _ -> }
            }
            // Create the AlertDialog
            builder.create()
        }
        alertDialog?.show()
    }

    companion object {
        fun newInstance(
            date: LocalDate,
            numEntries: Int?,
            isNewEntry: Boolean,
            resources: Resources
        ): EntryFragment {
            if (isNewEntry && numEntries == null) {
                throw IllegalArgumentException("New entries need to keep track of the total entries so far!")
            }

            val firstHintResource = if (date == LocalDate.now()) {
                R.string.what_are_you_thankful_for
            } else {
                R.string.what_were_you_thankful_for
            }

            val firstHint = resources.getString(firstHintResource)

            val prompts = resources.getStringArray(R.array.prompts)
            prompts.shuffle() //randomise prompts
            val quote = resources.getStringArray(R.array.inspirations).random()

            val fragment = EntryFragment()
            fragment.arguments =
                EntryArgs(
                    date.toString(),
                    isNewEntry,
                    numEntries,
                    quote,
                    firstHint,
                    prompts.toList()
                ).asMavericksArgs()
            return fragment
        }

        const val ENTRY_TO_SHARE = "ENTRY_TO_SHARE"
    }
}