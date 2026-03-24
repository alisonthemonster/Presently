package journal.gratitude.com.gratitudejournal.reminders.onboarding.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ShouldShowReminderOnboardingUseCaseTest {

    private val useCase = ShouldShowReminderOnboardingUseCase()

    @Test
    fun returnsTrue_onlyForUnseenBrandNewFirstEntry() {
        assertThat(
            useCase(
                savedBrandNewFirstEntry = true,
                hasSeenReminderOnboarding = false
            )
        ).isTrue()

        assertThat(
            useCase(
                savedBrandNewFirstEntry = false,
                hasSeenReminderOnboarding = false
            )
        ).isFalse()

        assertThat(
            useCase(
                savedBrandNewFirstEntry = true,
                hasSeenReminderOnboarding = true
            )
        ).isFalse()
    }
}
