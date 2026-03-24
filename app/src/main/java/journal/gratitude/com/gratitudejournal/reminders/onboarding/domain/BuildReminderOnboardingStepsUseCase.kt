package journal.gratitude.com.gratitudejournal.reminders.onboarding.domain

import android.os.Build
import javax.inject.Inject

class BuildReminderOnboardingStepsUseCase @Inject constructor() {

    operator fun invoke(snapshot: ReminderPermissionSnapshot): List<ReminderOnboardingStep> {
        return buildList {
            add(ReminderOnboardingStep.TIME)

            if (!snapshot.notificationsEnabled) {
                add(ReminderOnboardingStep.NOTIFICATIONS)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !snapshot.exactAlarmGranted) {
                add(ReminderOnboardingStep.EXACT_ALARM)
            }

            add(ReminderOnboardingStep.SUCCESS)
        }
    }
}
