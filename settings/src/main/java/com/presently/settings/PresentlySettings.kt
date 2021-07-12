package com.presently.settings

import org.threeten.bp.LocalTime

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

}

enum class BackupCadence {
    DAILY,
    WEEKLY,
    EVERY_CHANGE
}