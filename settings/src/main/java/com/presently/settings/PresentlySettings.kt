package com.presently.settings

import android.content.Context
import com.dropbox.core.oauth.DbxCredential
import kotlinx.datetime.LocalTime

interface PresentlySettings {

    fun getCurrentTheme(): String

    fun setTheme(themeName: String)

    fun isBiometricsEnabled(): Boolean

    fun shouldLockApp(): Boolean

    fun onAppBackgrounded()

    fun onAuthenticationSucceeded()

    fun shouldShowQuote(): Boolean

    fun getAutomaticBackupCadence(): BackupCadence

    fun getLocale(): String

    fun hasEnabledNotifications(): Boolean

    fun getNotificationTime(): LocalTime

    fun hasUserDisabledAlarmReminders(context: Context): Boolean

    fun getLinesPerEntryInTimeline(): Int

    fun shouldShowDayOfWeekInTimeline(): Boolean

    fun getAccessToken(): DbxCredential?

    fun setAccessToken(newToken: DbxCredential)

    fun wasDropboxAuthInitiated(): Boolean

    fun markDropboxAuthAsCancelled()

    fun markDropboxAuthInitiated()

    fun clearAccessToken()

    fun isOptedIntoAnalytics(): Boolean
}

enum class BackupCadence(val index: Int, val string: String) {
    DAILY(0, "Daily"),
    WEEKLY(1, "Weekly"),
    EVERY_CHANGE(2, "Every change")
}

