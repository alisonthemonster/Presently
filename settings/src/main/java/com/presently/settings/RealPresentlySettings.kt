package com.presently.settings

import android.app.AlarmManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.dropbox.core.oauth.DbxCredential
import com.presently.logging.AnalyticsLogger
import com.presently.logging.DROPBOX_AUTH_QUIT
import com.presently.logging.DROPBOX_AUTH_SUCCESS
import com.presently.settings.model.ACCESS_TOKEN
import com.presently.settings.model.ANALYTICS_OPT_IN_PREF
import com.presently.settings.model.APP_LANGUAGE
import com.presently.settings.model.BACKUP_CADENCE
import com.presently.settings.model.BACKUP_TOKEN
import com.presently.settings.model.DAY_OF_WEEK
import com.presently.settings.model.FINGERPRINT
import com.presently.settings.model.LINES_PER_ENTRY_IN_TIMELINE
import com.presently.settings.model.NOTIFS
import com.presently.settings.model.NOTIF_PREF_TIME
import com.presently.settings.model.NO_LANG_PREF
import com.presently.settings.model.ON_PAUSE_TIME
import com.presently.settings.model.SHOW_QUOTE
import com.presently.settings.model.THEME_PREF
import org.threeten.bp.LocalTime
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class RealPresentlySettings @Inject constructor(
    private val sharedPrefs: SharedPreferences,
    private val analytics: AnalyticsLogger,
) : PresentlySettings {

    override fun getCurrentTheme(): String {
        return sharedPrefs.getString(THEME_PREF, "original") ?: "original"
    }

    override fun setTheme(themeName: String) {
        sharedPrefs.edit()
            .putString(THEME_PREF, themeName)
            .apply()
        analytics.recordSelectEvent(themeName, "theme")
    }

    override fun isBiometricsEnabled(): Boolean {
        return sharedPrefs.getBoolean(FINGERPRINT, false)
    }

    override fun shouldLockApp(): Boolean {
        val lastPauseTime = sharedPrefs.getLong(ON_PAUSE_TIME, -1L)
        val currentTime = Date(System.currentTimeMillis()).time
        val diff = currentTime - lastPauseTime
        //if more than 5 minutes (300000ms) have passed since last destroy, lock out user
        return isBiometricsEnabled() && diff > 300000L
    }

    override fun onAppBackgrounded() {
        val date = Date(System.currentTimeMillis())
        sharedPrefs.edit().putLong(ON_PAUSE_TIME, date.time).apply()
    }

    override fun onAuthenticationSucceeded() {
        val date = Date(System.currentTimeMillis())
        sharedPrefs.edit().putLong(ON_PAUSE_TIME, date.time).apply()
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

    override fun hasUserDisabledAlarmReminders(context: Context): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //the user is on 12+ so we have to check if we still have permission
            !alarmManager.canScheduleExactAlarms()
        } else {
            //a pre 12 user cannot disable exact alarms
            false
        }
    }

    override fun getLinesPerEntryInTimeline(): Int {
        return sharedPrefs.getInt(LINES_PER_ENTRY_IN_TIMELINE, 10)
    }

    override fun shouldShowDayOfWeekInTimeline(): Boolean {
        return sharedPrefs.getBoolean(DAY_OF_WEEK, false)
    }

    override fun getAccessToken(): DbxCredential? {
        val serializedToken = sharedPrefs.getString(ACCESS_TOKEN, null)
        return when {
            serializedToken == "attempted" -> null
            serializedToken == null -> null
            serializedToken.contains("{") -> {
                //this user has a refresh token
                DbxCredential.Reader.readFully(serializedToken)
            }
            else -> {
                //this user has a long lived access token
                    //users who auth'd with Dropbox before
                DbxCredential(serializedToken)
            }
        }
    }

    override fun setAccessToken(newToken: DbxCredential) {
        analytics.recordEvent(DROPBOX_AUTH_SUCCESS)
        sharedPrefs.edit().putString(ACCESS_TOKEN, newToken.toString()).apply()
    }

    override fun markDropboxAuthInitiated() {
        sharedPrefs.edit().putString(ACCESS_TOKEN, "attempted").apply()
    }

    override fun wasDropboxAuthInitiated(): Boolean {
        val token = sharedPrefs.getString(ACCESS_TOKEN, null) ?: return false
        return token == "attempted"
    }

    override fun markDropboxAuthAsCancelled() {
        sharedPrefs.edit().putBoolean(BACKUP_TOKEN, false).apply() //reset the switch preference
        sharedPrefs.edit().remove(ACCESS_TOKEN).apply()
        analytics.recordEvent(DROPBOX_AUTH_QUIT)
    }

    override fun clearAccessToken() {
        sharedPrefs.edit().remove(ACCESS_TOKEN).apply()
        sharedPrefs.edit().putBoolean(BACKUP_TOKEN, false).apply()
    }

    override fun isOptedIntoAnalytics(): Boolean {
        return sharedPrefs.getBoolean(ANALYTICS_OPT_IN_PREF, true)
    }

    private fun getDeviceLanguage(): String {
        return Locale.getDefault().toLanguageTag()
    }

}