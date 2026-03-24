package journal.gratitude.com.gratitudejournal.reminders.onboarding.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class BuildReminderOnboardingStepsUseCaseTest {

    private val useCase = BuildReminderOnboardingStepsUseCase()

    @Test
    @Config(sdk = [30])
    fun buildsTimeNotificationsSuccess_whenNotificationsDisabledPreS() {
        val steps = useCase(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = false,
                exactAlarmGranted = true
            )
        )

        assertThat(steps).containsExactly(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.NOTIFICATIONS,
            ReminderOnboardingStep.SUCCESS
        ).inOrder()
    }

    @Test
    @Config(sdk = [35])
    fun buildsAllSteps_whenNotificationsAndExactAlarmNeedSetup() {
        val steps = useCase(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )

        assertThat(steps).containsExactly(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.NOTIFICATIONS,
            ReminderOnboardingStep.EXACT_ALARM,
            ReminderOnboardingStep.SUCCESS
        ).inOrder()
    }
}
