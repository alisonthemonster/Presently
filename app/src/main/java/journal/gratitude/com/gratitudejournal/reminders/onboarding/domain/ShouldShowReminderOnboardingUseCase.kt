package journal.gratitude.com.gratitudejournal.reminders.onboarding.domain

import javax.inject.Inject

class ShouldShowReminderOnboardingUseCase @Inject constructor() {
    operator fun invoke(
        savedBrandNewFirstEntry: Boolean,
        hasSeenReminderOnboarding: Boolean
    ): Boolean {
        return savedBrandNewFirstEntry && !hasSeenReminderOnboarding
    }
}
