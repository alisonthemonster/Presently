package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.presently.logging.AnalyticsLogger
import com.presently.logging.CrashReporter
import com.presently.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.calendar.CalendarAnimation
import journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarListener
import com.presently.ui.setStatusBarColorsForBackground
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.databinding.TimelineFragmentBinding
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.search.SearchFragment
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import org.threeten.bp.LocalDate
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class TimelineFragment : Fragment() {

    private val viewModel: TimelineViewModel by viewModels()
    @Inject lateinit var settings: PresentlySettings
    @Inject lateinit var analyticsLogger: AnalyticsLogger
    @Inject lateinit var crashReporter: CrashReporter

    private lateinit var adapter: TimelineAdapter

    private var _binding: TimelineFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.entryCalendar.isVisible) {
                    if (parentFragmentManager.backStackEntryCount > 0) {
                        parentFragmentManager.popBackStack()
                    } else {
                        //nothing left in back stack we can finish the activity
                        requireActivity().finish()
                    }
                } else {
                    val animation = CalendarAnimation(binding.calFab, binding.entryCalendar)
                    animation.closeCalendar()
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TimelineFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.timelineRecyclerView.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)

        val showDayOfWeek = settings.shouldShowDayOfWeekInTimeline()
        val linesPerEntry = settings.getLinesPerEntryInTimeline()
        adapter = TimelineAdapter(
            showDayOfWeek,
            linesPerEntry,
            object : OnClickListener {
                override fun onClick(
                    view: View,
                    clickedDate: LocalDate,
                    isNewEntry: Boolean,
                    numEntries: Int
                ) {
                    if (isNewEntry) {
                        analyticsLogger.recordEvent(CLICKED_NEW_ENTRY)
                    } else {
                        analyticsLogger.recordEvent(CLICKED_EXISTING_ENTRY)
                    }
                    navigateToDate(clickedDate, isNewEntry, numEntries)
                }
            })
        binding.timelineRecyclerView.adapter = adapter

        viewModel.entries.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })

        binding.overflowButton.setOnClickListener {
            PopupMenu(context, it).apply {
                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.notification_settings -> {
                            openSettings()
                            true
                        }
                        R.id.contact_us -> {
                            openContactForm()
                            true
                        }
                        else -> false
                    }
                }
                inflate(R.menu.overflow_menu)
                show()
            }
        }


        viewModel.datesWritten.observe(viewLifecycleOwner, Observer { dates ->
            binding.entryCalendar.setWrittenDates(dates)
        })

        binding.searchIcon.setOnClickListener {
            analyticsLogger.recordEvent(CLICKED_SEARCH)
            openSearchScreen()
        }

        binding.entryCalendar.setDayClickedListener(object : EntryCalendarListener {
            override fun onCloseClicked() {
                val animation = CalendarAnimation(binding.calFab, binding.entryCalendar)
                animation.closeCalendar()
            }

            override fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int) {
                if (isNewDate) {
                    analyticsLogger.recordEvent(CLICKED_NEW_ENTRY_CALENDAR)
                } else {
                    analyticsLogger.recordEvent(CLICKED_EXISTING_ENTRY_CALENDAR)
                }

                navigateToDate(date.toLocalDate(), isNewDate, numberOfEntries)
            }
        })

        binding.calFab.setOnClickListener {
            analyticsLogger.recordEvent(OPENED_CALENDAR)

            val animation = CalendarAnimation(binding.calFab, binding.entryCalendar)
            animation.openCalendar()
        }

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            v.updatePadding(
                top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top,
                bottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            )
            insets
        }

        val window = requireActivity().window
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.toolbarColor, typedValue, true)
        window.statusBarColor = typedValue.data
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openSearchScreen() {
        val fragment = SearchFragment()
        parentFragmentManager
            .beginTransaction()
            .addSharedElement(binding.searchIcon, "search_transition")
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SEARCH)
            .commit()
    }

    private fun navigateToDate(clickedDate: LocalDate, isNewEntry: Boolean, numEntries: Int) {
        val fragment = EntryFragment.newInstance(
            clickedDate,
            numEntries,
            isNewEntry,
            resources
        )
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_ENTRY)
            .commit()
    }

    private fun openContactForm() {
        analyticsLogger.recordEvent(OPENED_CONTACT_FORM)

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
        analyticsLogger.recordEvent(LOOKED_AT_SETTINGS)

        val fragment = SettingsFragment()
        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .addToBackStack(TIMELINE_TO_SETTINGS)
            .commit()
    }

    companion object {
        fun newInstance() = TimelineFragment()

        const val TIMELINE_TO_ENTRY = "TIMELINE_TO_ENTRY"
        const val TIMELINE_TO_SEARCH = "TIMELINE_TO_SEARCH"
        const val TIMELINE_TO_SETTINGS = "TIMELINE_TO_ENTRY"
    }
}

