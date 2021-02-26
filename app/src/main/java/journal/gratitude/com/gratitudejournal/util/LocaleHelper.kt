package journal.gratitude.com.gratitudejournal.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.APP_LANGUAGE
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.DEFAULT_APP_LANGUAGE
import java.util.*

object LocaleHelper {
    fun onAppAttached(context: Context): Context {
        return updateLanguage(context)
    }

    private fun updateLanguage(context: Context): Context {
        val language = getLastLanguageSaved(context)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return updateConfiguration(language, context)
        }else{
            return updateConfigurationLegacy(context, language)
        }
    }

    @Suppress("DEPRECATION")
    private fun updateConfigurationLegacy(
        context: Context,
        language: String
    ): Context {
        val configuration = getConfiguration(context, language)
        val resources = context.resources
        resources.updateConfiguration(configuration, resources.displayMetrics)

        return context
    }

    private fun updateConfiguration(
        language: String,
        context: Context
    ): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    private fun getLastLanguageSaved(context: Context): String {
        val lang = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(APP_LANGUAGE, DEFAULT_APP_LANGUAGE) ?: DEFAULT_APP_LANGUAGE
        return lang
    }

    private fun getConfiguration(
        context: Context,
        language: String
    ): Configuration {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val resources = context.resources
        val configuration = Configuration(resources.configuration)
        configuration.setLocale(locale)

        return configuration
    }
}