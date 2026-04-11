package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.reminders.onboarding.ui.DayOneDialogFragment
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment() {

    private val viewModel: TimelineViewModel by viewModels()
    @Inject lateinit var crashReporter: CrashReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                viewModel.onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme {
                    TimelineScreen(
                        state = viewModel.state,
                        onSearchClick = viewModel::onSearchClicked,
                        onOverflowClick = viewModel::onOverflowMenuClicked,
                        onOverflowDismiss = viewModel::onOverflowMenuDismissed,
                        onSettingsClick = viewModel::onSettingsClicked,
                        onContactClick = viewModel::onContactClicked,
                        onTimelineEntryClick = viewModel::onTimelineEntryClicked,
                        onCalendarClick = viewModel::onCalendarClicked,
                        onCalendarClose = viewModel::onCalendarClosed,
                        onCalendarDateClick = viewModel::onCalendarDateClicked
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            EntryFragment.REMINDER_ONBOARDING_TRIGGER_REQUEST_KEY,
            viewLifecycleOwner
        ) { _, bundle ->
            val savedBrandNewFirstEntry =
                bundle.getBoolean(EntryFragment.REMINDER_ONBOARDING_TRIGGER_RESULT_KEY, false)
            viewModel.onReminderOnboardingResult(savedBrandNewFirstEntry)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        TimelineEffect.ExitTimeline -> {
                            if (parentFragmentManager.backStackEntryCount > 0) {
                                parentFragmentManager.popBackStack()
                            } else {
                                requireActivity().finish()
                            }
                        }
                        is TimelineEffect.OpenEntry -> {
                            navigateToDate(
                                clickedDate = effect.clickedDate,
                                isNewEntry = effect.isNewEntry,
                                numEntries = effect.numberExistingEntries
                            )
                        }
                        TimelineEffect.OpenSearch -> openSearchScreen()
                        TimelineEffect.OpenSettings -> openSettings()
                        TimelineEffect.OpenContactForm -> openContactForm()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    if (state.showReminderOnboardingPrompt) {
                        openReminderOnboardingPrompt()
                        viewModel.onReminderOnboardingPromptHandled()
                    }
                }
            }
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        window.statusBarColor = typedValue.data
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onScreenResumed()
    }

    private fun openSearchScreen() {
        val fragment = SearchFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SEARCH)
            .commit()
    }

    private fun navigateToDate(
        clickedDate: org.threeten.bp.LocalDate,
        isNewEntry: Boolean,
        numEntries: Int
    ) {
        val fragment = EntryFragment.newInstance(
            date = clickedDate,
            numEntries = numEntries,
            isNewEntry = isNewEntry,
            resources = resources
        )
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_ENTRY)
            .commit()
    }

    private fun openContactForm() {
        val context = context ?: return
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")

            val emails = arrayOf("gratitude.journal.app@gmail.com")
            val subject = "In App Feedback"
            putExtra(Intent.EXTRA_EMAIL, emails)
            putExtra(Intent.EXTRA_SUBJECT, subject)

            val packageName = context.packageName
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            val text = """
                Device: ${Build.MODEL}
                OS Version: ${Build.VERSION.RELEASE}
                App Version: ${packageInfo.versionName}
                
                
                """.trimIndent()
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(intent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            crashReporter.logHandledException(activityNotFoundException)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSettings() {
        val fragment = SettingsFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SETTINGS)
            .commit()
    }

    private fun openReminderOnboardingPrompt() {
        if (
            parentFragmentManager.findFragmentByTag(DayOneDialogFragment.TAG)
                != null
        ) {
            return
        }

        DayOneDialogFragment()
            .show(parentFragmentManager, DayOneDialogFragment.TAG)
    }

    companion object {
        fun newInstance() = TimelineFragment()

        const val TIMELINE_TO_ENTRY = "TIMELINE_TO_ENTRY"
        const val TIMELINE_TO_SEARCH = "TIMELINE_TO_SEARCH"
        const val TIMELINE_TO_SETTINGS = "TIMELINE_TO_ENTRY"
        const val TIMELINE_TO_REMINDER_ONBOARDING = "TIMELINE_TO_REMINDER_ONBOARDING"
    }
}
