package journal.gratitude.com.gratitudejournal.ui.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.WebViewActivity
import journal.gratitude.com.gratitudejournal.WebViewActivity.Companion.WEBVIEW_PATH
import journal.gratitude.com.gratitudejournal.databinding.TimelineFragmentBinding
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.room.EntryDatabase
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment.Companion.DATE
import kotlinx.android.synthetic.main.timeline_fragment.*
import org.threeten.bp.LocalDate


class TimelineFragment : androidx.fragment.app.Fragment() {

    companion object {
        fun newInstance() = TimelineFragment()
    }

    private lateinit var viewModel: TimelineViewModel
    private lateinit var adapter: TimelineAdapter
    private lateinit var binding: TimelineFragmentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val entryDao = EntryDatabase.getDatabase(activity!!.application).entryDao()
        val repository = EntryRepository(entryDao) //TODO look into sharing across both fragments

        viewModel = ViewModelProviders.of(this, TimelineViewModelFactory(repository)).get(TimelineViewModel::class.java)
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

        timeline_recycler_view.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(context)
        adapter = TimelineAdapter(activity!!, object : TimelineAdapter.OnClickListener {
            override fun onClick(clickedDate: LocalDate) {
                val bundle = bundleOf(DATE to clickedDate.toString())
                findNavController().navigate(
                    R.id.action_timelineFragment_to_entryFragment,
                    bundle
                )
            }

        })
        timeline_recycler_view.adapter = adapter

        overflow_button.setOnClickListener{
            PopupMenu(context, it).apply {
                setOnMenuItemClickListener {item ->
                    when (item.itemId) {
                        R.id.notification_settings -> {
                            openNotificationSettings()
                            true
                        }
                        R.id.privacy_policy -> {
                            openPrivacyPolicy()
                            true
                        }
                        R.id.terms_conditions -> {
                            openTermsAndConditions()
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

        viewModel.entries.observe(this, Observer {
            binding.viewModel = viewModel
        })

    }

    private fun openContactForm() {
        val intent = Intent(Intent.ACTION_VIEW)
        val subject = "In App Feedback"
        val data = Uri.parse("mailto:gratitude.journal.app@gmail.com?subject=$subject")
        intent.data = data
        startActivity(intent)
    }

    private fun openTermsAndConditions() {
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra(WEBVIEW_PATH, "file:///android_asset/terms_and_conditions.html")
        }
        startActivity(intent)
    }

    private fun openPrivacyPolicy() {
        val intent = Intent(context, WebViewActivity::class.java).apply {
            putExtra(WEBVIEW_PATH, "file:///android_asset/privacy_policy.html")
        }
        startActivity(intent)
    }

    private fun openNotificationSettings() {
        findNavController().navigate(
            R.id.action_timelineFragment_to_settingsFragment)
    }


}
