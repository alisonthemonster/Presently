package journal.gratitude.com.gratitudejournal.ui.entry

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.text.Editable
import android.util.TypedValue
import android.view.View
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.airbnb.mvrx.MavericksView
import com.airbnb.mvrx.fragmentViewModel
import com.airbnb.mvrx.withState
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.util.backups.UploadToCloudWorker
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.util.textChanges
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.android.synthetic.main.entry_mvrx_fragment.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate

class EntryMvrxFragment : Fragment(R.layout.entry_mvrx_fragment), MavericksView {

    private val viewModel: EntryMvrxViewModel by fragmentViewModel()

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    val args: EntryMvrxFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val passedInDate = args.date
        viewModel.setDate(passedInDate)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                withState(viewModel, {
                    val isEdited = it.hasUserEdits
                    val isEmpty = it.isEmpty
                    if (isEdited && !isEmpty) {
                        showUnsavedEntryDialog()
                    } else {
                        findNavController().navigateUp()
                    }
                })
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireContext())

        prepareWindow()

        prompt_button.setOnClickListener {
            val v = it as ImageView
            val d = v.drawable as Animatable?
            d?.start()

            firebaseAnalytics.logEvent(CLICKED_PROMPT, null)

            viewModel.changePrompt()
        }

        share_button.setOnClickListener {
            firebaseAnalytics.logEvent(SHARED_ENTRY, null)
            withState(viewModel, {
                val message = it.entryContent
                val share = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(Intent.EXTRA_TEXT, message)

                startActivity(Intent.createChooser(share, getString(R.string.share_progress)))
            })
        }

        save_button.setOnClickListener {
            saveEntry()
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
                firebaseAnalytics.logEvent(COPIED_QUOTE, null)
                Toast.makeText(context, R.string.copied, Toast.LENGTH_SHORT).show()
            })
            true
        }

        lifecycleScope.launch {
            entry_text.textChanges()
                .debounce(300)
                .map { charSequence -> charSequence.toString() }
                .drop(1)
                .collectLatest {
                    viewModel.onTextChanged(it)
                }
        }
    }

    override fun invalidate() = withState(viewModel) { state ->
        when (state.date) {
            LocalDate.now() -> {
                date.text = resources.getString(R.string.today)
                thankful_for.text = resources.getString(R.string.iam)
            }
            LocalDate.now().minusDays(1) -> {
                date.text = resources.getString(R.string.yesterday)
                thankful_for.text = resources.getString(R.string.iwas)
            }
            else -> {
                date.text = state.date.toFullString()
                thankful_for.text = resources.getString(R.string.iwas)
            }
        }
        inspiration.text = state.quote
        entry_text.hint = state.hint ?: getHintString(state.date)
        setEditText(state.entryContent)
        share_button.visibility = if (state.isEmpty) View.GONE else View.VISIBLE
        prompt_button.visibility = if (state.isEmpty) View.VISIBLE else View.GONE
    }

    private fun getHintString(date: LocalDate): CharSequence? {
        return if (date == LocalDate.now()) {
            resources.getString(R.string.what_are_you_thankful_for)
        } else {
            resources.getString(R.string.what_were_you_thankful_for)
        }
    }

    private fun setEditText(newText: String) {
        val oldText = entry_text.text.toString()
        if (newText != oldText && newText.isNotEmpty()) {
            entry_text.setText(newText)
            entry_text.setSelection(newText.length)
        }
    }

    private fun prepareWindow() {
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
    }

    private fun saveEntry() {
        val numEntries = args.numEntries
        val isNewEntry = args.isNewEntry

        if (isNewEntry) {
            val bundle = Bundle()
            bundle.putInt(FirebaseAnalytics.Param.LEVEL, (numEntries + 1))

            firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LEVEL_UP, bundle)
            if (Milestone.milestones.contains(numEntries + 1)) {
                CelebrateDialogFragment.newInstance(numEntries + 1)
                    .show(requireFragmentManager(), "CelebrateDialogFragment")
            }
        } else {
            firebaseAnalytics.logEvent(EDITED_EXISTING_ENTRY, null)
        }

        viewModel.saveEntry()
        val imm =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(entry_text.windowToken, 0)

        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val accessToken = sharedPrefs.getString("access-token", null)
        val cadence = sharedPrefs.getString(SettingsFragment.BACKUP_CADENCE, "0") ?: "0"
        if (accessToken != null && cadence == "2") {
            val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadToCloudWorker>()
                .addTag(DropboxUploader.PRESENTLY_BACKUP)
                .build()
            WorkManager.getInstance(requireContext()).enqueue(uploadWorkRequest)
        }

        findNavController().navigateUp()
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