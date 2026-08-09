package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.sharing.view.SharingFragment
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import journal.gratitude.com.gratitudejournal.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class EntryFragment : Fragment() {

    private val viewModel: EntryViewModel by viewModels()

    @Inject
    lateinit var settings: PresentlySettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    viewModel.onBackPressed()
                }
            }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme {
                    EntryScreen(
                        state = viewModel.state,
                        onPromptClick = viewModel::onPromptClicked,
                        onShareClick = viewModel::onShareClicked,
                        onSaveClick = viewModel::saveEntry,
                        onQuoteLongClick = viewModel::onQuoteLongClicked,
                        onTextChanged = viewModel::onTextChanged
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareWindow()
        viewModel.onScreenShown()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is EntryEffect.CopyQuote -> copyQuote(effect.quote)
                        is EntryEffect.OpenShare -> {
                            openSharingScreen(
                                entryContent = effect.entryContent,
                                entryDate = effect.date.toDisplayDate(resources)
                            )
                        }
                        EntryEffect.ShowUnsavedChangesDialog -> showUnsavedEntryDialog()
                        is EntryEffect.EntrySaved -> {
                            onEntrySaved(effect.shouldTriggerReminderOnboarding)
                            if (effect.milestoneNumber != 0) {
                                CelebrateDialogFragment.newInstance(effect.milestoneNumber)
                                    .show(requireActivity().supportFragmentManager, "CelebrateDialogFragment")
                            }
                        }
                        EntryEffect.NavigateBack -> {
                            parentFragmentManager.popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun prepareWindow() {
        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.timelineBackgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    private fun openSharingScreen(entryContent: String, entryDate: String) {
        val fragment = SharingFragment.newInstance(entryDate, entryContent)
        requireActivity().supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(ENTRY_TO_SHARE)
            .commit()
    }

    private fun copyQuote(quote: String) {
        val clipboard =
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText("Gratitude quote", quote))
        Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboardInternal() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(view?.windowToken, 0)
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

    private fun onEntrySaved(shouldTriggerReminderOnboarding: Boolean) {
        hideKeyboardInternal()
        backupEntryIfNeeded()
        emitEntrySavedResultIfNeeded(shouldTriggerReminderOnboarding)
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun emitEntrySavedResultIfNeeded(shouldTriggerReminderOnboarding: Boolean) {
        requireActivity().supportFragmentManager.setFragmentResult(
            REMINDER_ONBOARDING_TRIGGER_REQUEST_KEY,
            bundleOf(
                REMINDER_ONBOARDING_TRIGGER_RESULT_KEY to shouldTriggerReminderOnboarding
            )
        )
    }

    private fun showUnsavedEntryDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(R.string.are_you_sure)
            .setMessage(R.string.unsaved_text)
            .setPositiveButton(R.string.continue_to_exit) { _, _ ->
                viewModel.onDiscardChangesConfirmed()
            }
            .setNegativeButton(R.string.cancel, null)
            .create()
        dialog.show()
    }

    companion object {
        const val ENTRY_ARGS_KEY = "entry_args"

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

            val prompts = resources.getStringArray(R.array.prompts)
            prompts.shuffle()

            return EntryFragment().apply {
                arguments = bundleOf(
                    ENTRY_ARGS_KEY to EntryArgs(
                        date = date.toString(),
                        isNewEntry = isNewEntry,
                        numberExistingEntries = numEntries,
                        quote = resources.getStringArray(R.array.inspirations).random(),
                        firstHint = resources.getString(firstHintResource),
                        prompts = prompts.toList()
                    )
                )
            }
        }

        const val ENTRY_TO_SHARE = "ENTRY_TO_SHARE"
        const val REMINDER_ONBOARDING_TRIGGER_REQUEST_KEY = "reminder_onboarding_trigger_request"
        const val REMINDER_ONBOARDING_TRIGGER_RESULT_KEY = "saved_brand_new_first_entry"
    }
}

private fun LocalDate.toDisplayDate(resources: Resources): String = when (toEntryHeaderMode()) {
    EntryHeaderMode.TODAY -> resources.getString(R.string.today)
    EntryHeaderMode.YESTERDAY -> resources.getString(R.string.yesterday)
    EntryHeaderMode.PAST -> toFullString()
}
