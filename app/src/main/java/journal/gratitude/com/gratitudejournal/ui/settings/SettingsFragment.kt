package journal.gratitude.com.gratitudejournal.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreference
import journal.gratitude.com.gratitudejournal.util.reminders.TimePreferenceFragment

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            "current_theme" -> {
                activity?.recreate()
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

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }


    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun openTermsAndConditions() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/presently-terms-conditions/home"))
        startActivity(browserIntent)
    }

    private fun openPrivacyPolicy() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/presently-privacy-policy/home"))
        startActivity(browserIntent)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SettingsFragment()
    }
}
