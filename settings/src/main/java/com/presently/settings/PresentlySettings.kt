package com.presently.settings

import com.dropbox.core.oauth.DbxCredential
import java.time.LocalTime

interface PresentlySettings {

    fun getCurrentTheme(): String

    fun setTheme(themeName: String)

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

    fun getAccessToken(): DbxCredential?

    fun setAccessToken(newToken: DbxCredential)

    fun wasDropboxAuthInitiated(): Boolean

    fun markDropboxAuthAsCancelled()

    fun markDropboxAuthInitiated()

    fun clearAccessToken()
}

enum class BackupCadence(val index: Int, val string: String) {
    DAILY(0, "Daily"),
    WEEKLY(1, "Weekly"),
    EVERY_CHANGE(2, "Every change")
}