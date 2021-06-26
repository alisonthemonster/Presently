package com.presently.settings

import android.content.SharedPreferences
import java.util.*
import org.threeten.bp.LocalTime

interface PresentlySettings {

    fun getCurrentTheme(): String

    fun isBiometricsEnabled(): Boolean

    fun shouldLockApp(): Boolean

    fun setOnPauseTime()

    fun getFirstDayOfWeek(): Int

    fun shouldShowQuote(): Boolean

    fun getAutomaticBackupCadence(): BackupCadence

    fun getLocale(): String

    fun hasEnabledNotifications(): Boolean

    fun getNotificationTime(): LocalTime

    fun getLinesPerEntryInTimeline(): Int

    fun shouldShowDayOfWeekInTimeline(): Boolean
}

enum class BackupCadence {
    DAILY,
    WEEKLY,
    EVERY_CHANGE
}

class RealPresentlySettings(private val sharedPrefs: SharedPreferences): PresentlySettings {
    override fun getCurrentTheme(): String {
        return sharedPrefs.getString(THEME_PREF, "original") ?: "original"
    }

    override fun isBiometricsEnabled(): Boolean {
        return sharedPrefs.getBoolean(FINGERPRINT, false)
    }

    override fun shouldLockApp(): Boolean {
        val lastDestroyTime = sharedPrefs.getLong(ON_PAUSE_TIME, -1L)
        val currentTime = Date(System.currentTimeMillis()).time
        val diff = currentTime - lastDestroyTime
        //if more than 5 minutes (300000ms) have passed since last destroy, lock out user
        return diff > 300000L
    }

    override fun setOnPauseTime() {
        val date = Date(System.currentTimeMillis())
        sharedPrefs.edit().putLong(ON_PAUSE_TIME, date.time).apply()
    }

    override fun getFirstDayOfWeek(): Int {
        return when(sharedPrefs.getString(FIRST_DAY_OF_WEEK, "monday")) {
            "0" -> Calendar.SATURDAY
            "1" -> Calendar.SUNDAY
            else -> Calendar.MONDAY
        }
    }

    override fun shouldShowQuote(): Boolean {
        return sharedPrefs.getBoolean(SHOW_QUOTE, true)
    }

    override fun getAutomaticBackupCadence(): BackupCadence {
        return when (sharedPrefs.getString(BACKUP_CADENCE, "0") ?: "0") {
            "0" -> BackupCadence.DAILY
            "1" -> BackupCadence.WEEKLY
            else -> BackupCadence.EVERY_CHANGE
        }
    }

    override fun getLocale(): String {
        val languagePref = sharedPrefs.getString(APP_LANGUAGE, NO_LANG_PREF) ?: NO_LANG_PREF
        return if (languagePref == NO_LANG_PREF) {
            getDeviceLanguage()
        } else {
            languagePref
        }
    }

    override fun hasEnabledNotifications(): Boolean {
        return sharedPrefs.getBoolean(NOTIFS, true)
    }

    override fun getNotificationTime(): LocalTime {
        val prefTime = sharedPrefs.getString(NOTIF_PREF_TIME, "21:00")
        return LocalTime.parse(prefTime)
    }

    override fun getLinesPerEntryInTimeline(): Int {
        return sharedPrefs.getInt(LINES_PER_ENTRY_IN_TIMELINE, 10)
    }

    override fun shouldShowDayOfWeekInTimeline(): Boolean {
        return sharedPrefs.getBoolean(DAY_OF_WEEK, false) ?: false
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().toLanguageTag()
    }

    companion object {
        const val BACKUP_TOKEN = "dropbox_pref"
        const val BACKUP_CADENCE = "dropbox_cadence"
        const val FINGERPRINT = "fingerprint_lock"
        const val NOTIFS = "notif_parent"
        const val NOTIF_PREF_TIME = "pref_time"
        const val THEME_PREF = "current_theme"
        const val SHOW_QUOTE = "show_quote"
        const val LINES_PER_ENTRY_IN_TIMELINE = "lines_per_entry_in_timeline"
        const val FIRST_DAY_OF_WEEK = "first_day_of_week"
        const val APP_LANGUAGE = "app_language"
        const val DAY_OF_WEEK = "day_of_week"
        const val NO_LANG_PREF = "no_language_selected"
        const val ON_PAUSE_TIME = "last_destroy_time"
    }

}