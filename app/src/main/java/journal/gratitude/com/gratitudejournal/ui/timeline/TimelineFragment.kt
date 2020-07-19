package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.android.support.DaggerFragment
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.databinding.TimelineFragmentBinding
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.calendar.CalendarAnimation
import journal.gratitude.com.gratitudejournal.ui.calendar.EntryCalendarListener
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment.Companion.DATE
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment.Companion.IS_NEW_ENTRY
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment.Companion.NUM_ENTRIES
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.DAY_OF_WEEK
import journal.gratitude.com.gratitudejournal.util.backups.ExportCallback
import journal.gratitude.com.gratitudejournal.util.toLocalDate
import kotlinx.android.synthetic.main.timeline_fragment.*
import org.threeten.bp.LocalDate
import java.io.File
import java.util.*
import javax.inject.Inject


class TimelineFragment : DaggerFragment() {

    companion object {
        fun newInstance() = TimelineFragment()
        const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL = 512
        const val IMPORT_CSV = 206
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel by viewModels<TimelineViewModel> { viewModelFactory }

    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: TimelineFragmentBinding
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (entry_calendar?.isVisible != true) {
                    if (!findNavController().navigateUp()) {
                        //nothing left in back stack we can finish the activity
                        requireActivity().finish()
                    }
                } else {
                    val animation = CalendarAnimation(cal_fab, entry_calendar)
                    animation.closeCalendar()
                }
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
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

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)

        timeline_recycler_view.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(context)
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val showDayOfWeek = sharedPrefs?.getBoolean(DAY_OF_WEEK, false) ?: false
        adapter = TimelineAdapter(activity!!, showDayOfWeek, object : TimelineAdapter.OnClickListener {
            override fun onClick(
                clickedDate: LocalDate,
                isNewEntry: Boolean,
                numEntries: Int
            ) {
                if (isNewEntry) {
                    firebaseAnalytics.logEvent(CLICKED_NEW_ENTRY, null)
                } else {
                    firebaseAnalytics.logEvent(CLICKED_EXISTING_ENTRY, null)
                }
                navigateToDate(clickedDate, isNewEntry, numEntries)
            }
        })
        timeline_recycler_view.adapter = adapter

        overflow_button.setOnClickListener {
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

        viewModel.entries.observe(viewLifecycleOwner, Observer {
            binding.viewModel = viewModel
        })

        viewModel.datesWritten.observe(viewLifecycleOwner, Observer { dates ->
            entry_calendar.setWrittenDates(dates)
        })

        search_icon.setOnClickListener {
            firebaseAnalytics.logEvent(CLICKED_SEARCH, null)

            val action = TimelineFragmentDirections.actionTimelineFragmentToSearchFragment()
            val extras = FragmentNavigatorExtras(
                search_icon to "search_transition"
            )


            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.timelineFragment) {
                navController.navigate(action, extras)
            }
        }

        entry_calendar.setDayClickedListener(object : EntryCalendarListener {
            override fun onCloseClicked() {
                val animation = CalendarAnimation(cal_fab, entry_calendar)
                animation.closeCalendar()
            }

            override fun onDateClicked(date: Date, isNewDate: Boolean, numberOfEntries: Int) {
                if (isNewDate) {
                    firebaseAnalytics.logEvent(CLICKED_NEW_ENTRY_CALENDAR, null)
                } else {
                    firebaseAnalytics.logEvent(CLICKED_EXISTING_ENTRY_CALENDAR, null)
                }

                navigateToDate(date.toLocalDate(), isNewDate, numberOfEntries)
            }
        })

        cal_fab.setOnClickListener {
            firebaseAnalytics.logEvent(OPENED_CALENDAR, null)

            val animation = CalendarAnimation(cal_fab, entry_calendar)
            animation.openCalendar()
        }
    }

    private fun navigateToDate(clickedDate: LocalDate, isNewEntry: Boolean, numEntries: Int) {
        val bundle = bundleOf(
            DATE to clickedDate.toString(),
            IS_NEW_ENTRY to isNewEntry,
            NUM_ENTRIES to numEntries
        )
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.timelineFragment) {
            navController.navigate(
                R.id.action_timelineFragment_to_entryFragment,
                bundle
            )
        }
    }

    private fun openContactForm() {
        firebaseAnalytics.logEvent(OPENED_CONTACT_FORM, null)

        val intent = Intent(Intent.ACTION_VIEW)
        val subject = "In App Feedback"
        val data = Uri.parse("mailto:gratitude.journal.app@gmail.com?subject=$subject")
        intent.data = data
        try {
            startActivity(intent)
        } catch (activityNotFoundException: ActivityNotFoundException) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(activityNotFoundException)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSettings() {
        firebaseAnalytics.logEvent(LOOKED_AT_SETTINGS, null)

        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.timelineFragment) {
            navController.navigate(
                R.id.action_timelineFragment_to_settingsFragment
            )
        }
    }

    private val exportCallback: ExportCallback = object : ExportCallback {
        override fun onSuccess(file: File) {
            Snackbar.make(container, R.string.export_success, Snackbar.LENGTH_LONG)
                .setAction(R.string.open) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        val apkURI = FileProvider.getUriForFile(
                            context!!,
                            context?.applicationContext?.packageName + ".provider", file
                        )
                        intent.setDataAndType(apkURI, "text/csv")
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        val crashlytics = FirebaseCrashlytics.getInstance()
                        crashlytics.recordException(e)
                        Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
                    }
                }.show()
        }

        override fun onFailure(exception: Exception) {
            val crashlytics = FirebaseCrashlytics.getInstance()
            crashlytics.recordException(exception)
            Toast.makeText(context, "Error : ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

}

