package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreference
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreferenceFragment
import android.content.SharedPreferences
import android.view.View
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.model.*
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var firebaseAnalytics: FirebaseAnalytics


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        val privacy = findPreference("privacy_policy")
        privacy.setOnPreferenceClickListener {
            openPrivacyPolicy()
            true
        }
        val terms = findPreference("terms_conditions")
        terms.setOnPreferenceClickListener {
            openTermsAndConditions()
            true
        }
        val faq = findPreference("faq")
        faq.setOnPreferenceClickListener {
            openFaq()
            true
        }
        val theme = findPreference("current_theme")
        theme.setOnPreferenceClickListener {
            openThemes()
            true
        }
    }

    override fun onResume() {
        super.onResume()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        // Set up a listener whenever a key changes
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "current_theme" -> {
                val theme = sharedPreferences.getString("current_theme", "original")
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, theme)
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, theme)
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "theme")
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                activity?.recreate()
            }
            "notif_parent" -> {
                val notifsTurnedOn = sharedPreferences.getBoolean(key, true)
                if (notifsTurnedOn) {
                    NotificationScheduler().configureNotifications(context!!)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "true")
                } else {
                    NotificationScheduler().disableNotifications(context!!)
                    firebaseAnalytics.logEvent(CANCELLED_NOTIFS, null)
                    firebaseAnalytics.setUserProperty(HAS_NOTIFICATIONS_TURNED_ON, "false")
                }
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        var dialogFragment: DialogFragment? = null
        if (preference is TimePreference) {
            dialogFragment =
                TimePreferenceFragment()
            val bundle = Bundle(1)
            bundle.putString("key", preference.getKey())
            dialogFragment.setArguments(bundle)
        }

        if (dialogFragment != null) {
            dialogFragment.setTargetFragment(this, 0)
            dialogFragment.show(this.fragmentManager!!, "DIALOG")
        } else {
            super.onDisplayPreferenceDialog(preference)
        }
    }

    private fun openThemes() {
        firebaseAnalytics.logEvent(OPENED_THEMES, null)

        findNavController().navigate(
                R.id.action_settingsFragment_to_themeFragment)
    }

    private fun openTermsAndConditions() {
        firebaseAnalytics.logEvent(OPENED_TERMS_CONDITIONS, null)

        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://presently-app.firebaseapp.com/termsconditions.html"))
        startActivity(browserIntent)
    }

    private fun openPrivacyPolicy() {
        firebaseAnalytics.logEvent(OPENED_PRIVACY_POLICY, null)

        val browserIntent =
            Intent(Intent.ACTION_VIEW, Uri.parse("https://presently-app.firebaseapp.com/privacypolicy.html"))
        startActivity(browserIntent)
    }

    private fun openFaq() {
        firebaseAnalytics.logEvent(OPENED_FAQ, null)

        val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://presently-app.firebaseapp.com/faq.html"))
        startActivity(browserIntent)
    }
}
