package journal.gratitude.com.gratitudejournal.util

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import journal.gratitude.com.gratitudejournal.settings.model.APP_LANGUAGE
import journal.gratitude.com.gratitudejournal.settings.model.NO_LANG_PREF

object AppLocaleManager {

    fun applyStoredApplicationLocales(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return
        }

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val languagePreference = sharedPreferences.getString(APP_LANGUAGE, NO_LANG_PREF)
        val locales = localeListFor(languagePreference)
        if (AppCompatDelegate.getApplicationLocales() != locales) {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    fun localeListFor(languagePreference: String?): LocaleListCompat {
        return if (languagePreference.isNullOrBlank() || languagePreference == NO_LANG_PREF) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languagePreference)
        }
    }
}
