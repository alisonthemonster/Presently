package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import androidx.compose.ui.test.junit4.createComposeRule
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.testUtils.ScreenshotTest
import journal.gratitude.com.gratitudejournal.testUtils.captureInTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.categories.Category
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.threeten.bp.LocalTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
@Category(ScreenshotTest::class)
class ReminderOnboardingScreenRoborazziTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun timeStep_originalTheme() {
        captureScenario(
            scenario = "1-time-step",
            state = ReminderOnboardingState(
                selectedTime = LocalTime.of(8, 30)
            )
        )
    }

    @Test
    fun notificationStep_originalTheme() {
        captureScenario(
            scenario = "2-notification-step",
            state = notificationStepState()
        )
    }

    @Test
    fun notificationStepDenied_originalTheme() {
        captureScenario(
            scenario = "2b-notification-step-denied",
            state = notificationStepState(
                notificationPermissionDenied = true
            )
        )
    }

    @Test
    fun exactAlarmStep_originalTheme() {
        captureScenario(
            scenario = "3-exact-alarm-step",
            state = exactAlarmStepState()
        )
    }

    @Test
    fun exactAlarmStepDenied_originalTheme() {
        captureScenario(
            scenario = "3b-exact-alarm-step-denied",
            state = exactAlarmStepState(
                exactAlarmPermissionDenied = true
            )
        )
    }

    @Test
    fun successStep_originalTheme() {
        captureScenario(
            scenario = "4-success-step",
            state = ReminderOnboardingState(
                selectedTime = LocalTime.of(7, 5),
                currentStep = ReminderOnboardingStep.SUCCESS
            ),
            successAnimationProgressOverride = 1f
        )
    }

    private fun captureScenario(
        scenario: String,
        state: ReminderOnboardingState,
        successAnimationProgressOverride: Float? = null
    ) {
        composeRule.captureInTheme(
            screen = "reminder-onboarding",
            scenario = scenario,
            themeSpec = PresentlyThemeSpec.Original
        ) {
            ReminderOnboardingScreenContent(
                state = state,
                onDismiss = {},
                onPrimaryAction = {},
                onSkipForNow = {},
                onTimeChanged = {},
                successAnimationProgressOverride = successAnimationProgressOverride
            )
        }
    }

    private fun notificationStepState(
        notificationPermissionDenied: Boolean = false
    ) = ReminderOnboardingState(
        currentStep = ReminderOnboardingStep.NOTIFICATIONS,
        steps = listOf(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.NOTIFICATIONS,
            ReminderOnboardingStep.SUCCESS
        ),
        notificationPermissionDenied = notificationPermissionDenied
    )

    private fun exactAlarmStepState(
        exactAlarmPermissionDenied: Boolean = false
    ) = ReminderOnboardingState(
        currentStep = ReminderOnboardingStep.EXACT_ALARM,
        steps = listOf(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.EXACT_ALARM,
            ReminderOnboardingStep.SUCCESS
        ),
        exactAlarmPermissionDenied = exactAlarmPermissionDenied
    )
}
