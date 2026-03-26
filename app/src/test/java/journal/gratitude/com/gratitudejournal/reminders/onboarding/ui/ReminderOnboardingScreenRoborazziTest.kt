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
        composeRule.captureInTheme(
            screen = "reminder-onboarding",
            scenario = "1-time-step",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            ReminderOnboardingScreenContent(
                state = ReminderOnboardingState(
                    selectedTime = LocalTime.of(8, 30)
                ),
                onDismiss = {},
                onPrimaryAction = {},
                onSkipForNow = {},
                onTimeChanged = {}
            )
        }
    }

    @Test
    fun notificationStep_originalTheme() {
        composeRule.captureInTheme(
            screen = "reminder-onboarding",
            scenario = "2-notification-step",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            ReminderOnboardingScreenContent(
                state = ReminderOnboardingState(
                    currentStep = ReminderOnboardingStep.NOTIFICATIONS,
                    steps = listOf(
                        ReminderOnboardingStep.TIME,
                        ReminderOnboardingStep.NOTIFICATIONS,
                        ReminderOnboardingStep.SUCCESS
                    )
                ),
                onDismiss = {},
                onPrimaryAction = {},
                onSkipForNow = {},
                onTimeChanged = {}
            )
        }
    }

    @Test
    fun exactAlarmStep_originalTheme() {
        composeRule.captureInTheme(
            screen = "reminder-onboarding",
            scenario = "3-exact-alarm-step",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            ReminderOnboardingScreenContent(
                state = ReminderOnboardingState(
                    currentStep = ReminderOnboardingStep.EXACT_ALARM,
                    steps = listOf(
                        ReminderOnboardingStep.TIME,
                        ReminderOnboardingStep.EXACT_ALARM,
                        ReminderOnboardingStep.SUCCESS
                    )
                ),
                onDismiss = {},
                onPrimaryAction = {},
                onSkipForNow = {},
                onTimeChanged = {}
            )
        }
    }

    @Test
    fun successStep_originalTheme() {
        composeRule.captureInTheme(
            screen = "reminder-onboarding",
            scenario = "4-success-step",
            themeSpec = PresentlyThemeSpec.Original
        ) {
            ReminderOnboardingScreenContent(
                state = ReminderOnboardingState(
                    selectedTime = LocalTime.of(7, 5),
                    currentStep = ReminderOnboardingStep.SUCCESS
                ),
                onDismiss = {},
                onPrimaryAction = {},
                onSkipForNow = {},
                onTimeChanged = {},
                successAnimationProgressOverride = 1f
            )
        }
    }
}
