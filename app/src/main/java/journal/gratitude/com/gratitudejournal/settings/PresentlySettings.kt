package journal.gratitude.com.gratitudejournal.settings

import android.content.Context
import org.threeten.bp.LocalTime

interface PresentlySettings {

    fun getCurrentTheme(): String

    fun setTheme(themeName: String)

    fun isBiometricsEnabled(): Boolean

    fun shouldLockApp(): Boolean

    fun setOnPauseTime()

    fun getFirstDayOfWeek(): Int

    fun shouldShowQuote(): Boolean

    fun getLocale(): String

    fun hasEnabledNotifications(): Boolean

    fun setNotificationsEnabled(enabled: Boolean)

    fun getNotificationTime(): LocalTime

    fun setNotificationTime(time: LocalTime)

    fun hasUserDisabledAlarmReminders(context: Context): Boolean

    fun hasSeenReminderOnboarding(): Boolean

    fun markReminderOnboardingSeen()

    fun clearReminderOnboardingSeen()

    fun hasRequestedNotificationPermission(): Boolean

    fun markNotificationPermissionRequested()

    fun getLinesPerEntryInTimeline(): Int

    fun shouldShowDayOfWeekInTimeline(): Boolean

    fun isOptedIntoAnalytics(): Boolean
}
