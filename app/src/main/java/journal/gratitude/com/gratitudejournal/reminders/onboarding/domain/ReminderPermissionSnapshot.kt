package journal.gratitude.com.gratitudejournal.reminders.onboarding.domain

data class ReminderPermissionSnapshot(
    val notificationsEnabled: Boolean,
    val canRequestNotificationPermission: Boolean,
    val exactAlarmGranted: Boolean
)
