package journal.gratitude.com.gratitudejournal.reminders.onboarding.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.util.reminders.NotificationScheduler
import org.threeten.bp.LocalTime
import javax.inject.Inject

class ReminderOnboardingRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: PresentlySettings
) {

    fun getNotificationTime(): LocalTime {
        return settings.getNotificationTime()
    }

    fun setNotificationTime(time: LocalTime) {
        settings.setNotificationTime(time)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        settings.setNotificationsEnabled(enabled)
    }

    fun hasSeenReminderOnboarding(): Boolean {
        return settings.hasSeenReminderOnboarding()
    }

    fun markReminderOnboardingSeen() {
        settings.markReminderOnboardingSeen()
    }

    fun clearReminderOnboardingSeen() {
        settings.clearReminderOnboardingSeen()
    }

    fun enableAndScheduleReminders() {
        settings.setNotificationsEnabled(true)
        NotificationScheduler().configureNotifications(context, settings)
    }
}
