package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import org.threeten.bp.LocalTime

data class ReminderOnboardingState(
    val selectedTime: LocalTime = LocalTime.parse("21:00"),
    val steps: List<ReminderOnboardingStep> = listOf(
        ReminderOnboardingStep.TIME,
        ReminderOnboardingStep.SUCCESS
    ),
    val currentStep: ReminderOnboardingStep = ReminderOnboardingStep.TIME,
    val notificationPermissionDenied: Boolean = false,
    val exactAlarmPermissionDenied: Boolean = false,
    val isStarted: Boolean = false
) {
    val currentStepIndex: Int
        get() = steps.indexOf(currentStep).coerceAtLeast(0)
}

sealed interface ReminderOnboardingEffect {
    data object RequestNotificationPermission : ReminderOnboardingEffect
    data object OpenNotificationSettings : ReminderOnboardingEffect
    data object OpenExactAlarmSettings : ReminderOnboardingEffect
    data object Dismiss : ReminderOnboardingEffect
}
