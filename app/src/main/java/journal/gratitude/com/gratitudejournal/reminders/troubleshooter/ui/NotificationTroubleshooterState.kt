package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

data class NotificationTroubleshooterState(
    val isLoading: Boolean = true,
    val checks: List<NotificationTroubleshooterCheckResult> = emptyList()
) {
    val allChecksPass: Boolean
        get() = checks.isNotEmpty() && checks.all(NotificationTroubleshooterCheckResult::passed)
}

enum class NotificationTroubleshooterCheck {
    POST_NOTIFICATIONS,
    APP_NOTIFICATIONS,
    EXACT_ALARM;

    fun resultFor(passed: Boolean) = NotificationTroubleshooterCheckResult(
        check = this,
        passed = passed
    )

    val analyticsValue: String
        get() = when (this) {
            POST_NOTIFICATIONS -> "post_notifications"
            APP_NOTIFICATIONS -> "app_notifications"
            EXACT_ALARM -> "exact_alarm"
        }
}

data class NotificationTroubleshooterCheckResult(
    val check: NotificationTroubleshooterCheck,
    val passed: Boolean
)

data class SupportEmailData(
    val recipients: Array<String>,
    val subject: String,
    val body: String
)

sealed interface NotificationTroubleshooterEffect {
    data object OpenAppNotificationSettings : NotificationTroubleshooterEffect
    data object OpenExactAlarmSettings : NotificationTroubleshooterEffect
    data class ContactSupport(val emailData: SupportEmailData) : NotificationTroubleshooterEffect
}
