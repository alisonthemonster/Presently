package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class ReminderOnboardingScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notificationStep_requestable_showsAllowNotifications_andNoSkip() {
        setContent(notificationStepState())

        composeRule.onNodeWithTag("notification_step").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.turn_on_notifications)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.allow_notifications)).assertIsDisplayed()
        composeRule.onAllNodesWithTag("skip_for_now").assertCountEquals(0)
    }

    @Test
    fun notificationStep_denied_showsDeniedCopy_andNoSkip() {
        setContent(
            notificationStepState(
                notificationPermissionDenied = true
            )
        )

        composeRule.onNodeWithTag("notification_step").assertIsDisplayed()
        composeRule.onNodeWithText(
            string(R.string.notification_permission_is_required_for_reminders)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(
            string(R.string.presently_can_t_send_reminders_without_your_permission_please_try_again)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.open_settings)).assertIsDisplayed()
        composeRule.onAllNodesWithTag("skip_for_now").assertCountEquals(0)
    }

    @Test
    fun exactAlarmStep_requestable_showsPrompt_andNoSkip() {
        setContent(exactAlarmStepState())

        composeRule.onNodeWithTag("exact_alarm_step").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.allow_exact_alarms)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.open_settings)).assertIsDisplayed()
        composeRule.onAllNodesWithTag("skip_for_now").assertCountEquals(0)
    }

    @Test
    fun exactAlarmStep_denied_showsDeniedCopy_andSkip() {
        setContent(
            exactAlarmStepState(
                exactAlarmPermissionDenied = true
            )
        )

        composeRule.onNodeWithTag("exact_alarm_step").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.exact_alarms_are_still_blocked))
            .assertIsDisplayed()
        composeRule.onNodeWithText(
            string(R.string.without_granting_exact_alarm_permissions_notifications_may_be_delayed_or_skipped_by_your_phone)
        ).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.open_settings)).assertIsDisplayed()
        composeRule.onNodeWithTag("skip_for_now").assertIsDisplayed()
    }

    private fun setContent(state: ReminderOnboardingState) {
        composeRule.setContent {
            PresentlyTheme(themeSpec = PresentlyThemeSpec.Original) {
                ReminderOnboardingScreenContent(
                    state = state,
                    onDismiss = {},
                    onPrimaryAction = {},
                    onSkipForNow = {},
                    onTimeChanged = {}
                )
            }
        }
    }

    private fun string(resId: Int): String =
        ApplicationProvider.getApplicationContext<android.content.Context>().getString(resId)

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
