package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.reminders.onboarding.data.ReminderOnboardingRepository
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.BuildReminderOnboardingStepsUseCase
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderPermissionSnapshot
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.threeten.bp.LocalTime
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ReminderOnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeSettings: TestPresentlySettings
    private lateinit var analytics: TestAnalyticsLogger
    private lateinit var viewModel: ReminderOnboardingViewModel

    @Before
    fun setUp() {
        fakeSettings = TestPresentlySettings()
        analytics = TestAnalyticsLogger()
        viewModel = ReminderOnboardingViewModel(
            repository = ReminderOnboardingRepository(
                context = ApplicationProvider.getApplicationContext(),
                settings = fakeSettings
            ),
            analytics = analytics,
            buildReminderOnboardingSteps = BuildReminderOnboardingStepsUseCase()
        )
    }

    @Test
    fun timeSave_advancesToNotificationAndPersistsTime() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )

        val newTime = LocalTime.of(8, 45)
        viewModel.onTimeChanged(newTime)
        viewModel.onTimeSaved()

        assertThat(fakeSettings.notificationTimeValue).isEqualTo(newTime)
        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.NOTIFICATIONS)
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepCompleted"))
            .contains(mapOf("step" to "time"))
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepViewed"))
            .contains(mapOf("step" to "notifications"))
    }

    @Test
    fun start_initializesSteps_marksSeen_and_recordsViewed() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )

        assertThat(viewModel.state.value.isStarted).isTrue()
        assertThat(viewModel.state.value.steps).containsExactly(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.NOTIFICATIONS,
            ReminderOnboardingStep.SUCCESS
        ).inOrder()
        assertThat(fakeSettings.reminderOnboardingSeenValue).isTrue()
        assertThat(analytics.recordedEvents).contains("reminderOnboardingViewed")
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepViewed"))
            .containsExactly(mapOf("step" to "time"))
    }

    @Test
    fun start_secondInvocation_refreshesSteps_withoutRecordingViewedAgain() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )

        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = true,
                canRequestNotificationPermission = false,
                exactAlarmGranted = true
            )
        )

        assertThat(viewModel.state.value.steps).containsExactly(
            ReminderOnboardingStep.TIME,
            ReminderOnboardingStep.SUCCESS
        ).inOrder()
        assertThat(analytics.recordedEvents.count { it == "reminderOnboardingViewed" }).isEqualTo(1)
        assertThat(analytics.recordedEvents.count { it == "reminderOnboardingStepViewed" }).isEqualTo(1)
    }

    @Test
    fun onTimeChanged_updatesSelectedTime() = runTest {
        val newTime = LocalTime.of(6, 10)

        viewModel.onTimeChanged(newTime)

        assertThat(viewModel.state.value.selectedTime).isEqualTo(newTime)
    }

    @Test
    fun notificationDenial_keepsUserOnNotificationStepAndShowsDeniedState() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()

        viewModel.onNotificationPermissionDialogResult(false)

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.NOTIFICATIONS)
        assertThat(viewModel.state.value.notificationPermissionDenied).isTrue()
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingPermissionResult"))
            .contains(mapOf("permission" to "notifications", "result" to "denied"))
    }

    @Test
    fun onPrimaryActionClicked_onTimeStep_persistsTimeAndAdvances() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeChanged(LocalTime.of(7, 20))

        viewModel.onPrimaryActionClicked()

        assertThat(fakeSettings.notificationTimeValue).isEqualTo(LocalTime.of(7, 20))
        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.NOTIFICATIONS)
    }

    @Test
    fun onPrimaryActionClicked_onNotificationStep_emitsPermissionRequestEffect() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onPrimaryActionClicked()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.RequestNotificationPermission)
    }

    @Test
    fun onPrimaryActionClicked_onDeniedNotificationStep_opensSettings() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()
        viewModel.onNotificationPermissionDialogResult(false)

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onPrimaryActionClicked()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.OpenNotificationSettings)
    }

    @Test
    fun onNotificationSettingsResult_granted_advancesToSuccessWhenNoMoreStepsRemain() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = false,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()

        viewModel.onNotificationSettingsResult(true)

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.SUCCESS)
        assertThat(viewModel.state.value.notificationPermissionDenied).isFalse()
        assertThat(fakeSettings.notificationsEnabledValue).isTrue()
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingPermissionResult"))
            .contains(mapOf("permission" to "notifications", "result" to "granted"))
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepCompleted"))
            .contains(mapOf("step" to "notifications"))
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepViewed"))
            .contains(mapOf("step" to "success"))
    }

    @Test
    fun onNotificationSettingsResult_denied_staysOnNotificationStepAndShowsDeniedState() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = false,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()

        viewModel.onNotificationSettingsResult(false)

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.NOTIFICATIONS)
        assertThat(viewModel.state.value.notificationPermissionDenied).isTrue()
    }

    @Test
    @Config(sdk = [35])
    fun grantedPermissions_completeSetupAndMoveToSuccess() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )
        viewModel.onTimeSaved()

        viewModel.onNotificationPermissionDialogResult(true)
        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.EXACT_ALARM)

        viewModel.onExactAlarmSettingsResult(true)

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.SUCCESS)
        assertThat(fakeSettings.notificationsEnabledValue).isTrue()
        assertThat(analytics.recordedEvents).contains("reminderOnboardingCompleted")
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingPermissionResult"))
            .containsAtLeast(
                mapOf("permission" to "notifications", "result" to "granted"),
                mapOf("permission" to "exact_alarm", "result" to "granted")
            )
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepCompleted"))
            .containsAtLeast(
                mapOf("step" to "time"),
                mapOf("step" to "notifications"),
                mapOf("step" to "exact_alarm")
            )
        assertThat(analytics.recordedEventDetailsFor("reminderOnboardingStepViewed"))
            .containsAtLeast(
                mapOf("step" to "time"),
                mapOf("step" to "notifications"),
                mapOf("step" to "exact_alarm"),
                mapOf("step" to "success")
            )
    }

    @Test
    @Config(sdk = [35])
    fun onPrimaryActionClicked_onExactAlarmStep_opensExactAlarmSettings() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )
        viewModel.onTimeSaved()
        viewModel.onNotificationPermissionDialogResult(true)

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onPrimaryActionClicked()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.OpenExactAlarmSettings)
    }

    @Test
    @Config(sdk = [35])
    fun onExactAlarmSettingsResult_denied_staysOnExactAlarmStep_andShowsDeniedState() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )
        viewModel.onTimeSaved()
        viewModel.onNotificationPermissionDialogResult(true)

        viewModel.onExactAlarmSettingsResult(false)

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.EXACT_ALARM)
        assertThat(viewModel.state.value.exactAlarmPermissionDenied).isTrue()
    }

    @Test
    @Config(sdk = [35])
    fun skipForNow_onExactAlarmStep_advancesToSuccess_andEnablesReminders() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )
        viewModel.onTimeSaved()
        viewModel.onNotificationPermissionDialogResult(true)
        viewModel.onExactAlarmSettingsResult(false)

        viewModel.onSkipForNowClicked()

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.SUCCESS)
        assertThat(fakeSettings.notificationsEnabledValue).isTrue()
    }

    @Test
    @Config(sdk = [35])
    fun successState_isRetained_whenStartRefreshesStepsAgain() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )
        viewModel.onTimeSaved()
        viewModel.onNotificationPermissionDialogResult(true)
        viewModel.onSkipForNowClicked()

        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = false
            )
        )

        assertThat(viewModel.state.value.currentStep).isEqualTo(ReminderOnboardingStep.SUCCESS)
        assertThat(viewModel.state.value.steps).contains(ReminderOnboardingStep.SUCCESS)
    }

    @Test
    fun skipForNow_emitsDismissEffect() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onSkipForNowClicked()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.Dismiss)
    }

    @Test
    fun onDismissRequested_emitsDismissEffect() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = false,
                canRequestNotificationPermission = true,
                exactAlarmGranted = true
            )
        )

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onDismissRequested()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.Dismiss)
    }

    @Test
    fun onDoneClicked_emitsDismissEffect() = runTest {
        viewModel.start(
            ReminderPermissionSnapshot(
                notificationsEnabled = true,
                canRequestNotificationPermission = false,
                exactAlarmGranted = true
            )
        )
        viewModel.onTimeSaved()

        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }
        viewModel.onDoneClicked()

        assertThat(effect.await()).isEqualTo(ReminderOnboardingEffect.Dismiss)
    }

    private class TestAnalyticsLogger : AnalyticsLogger {
        val recordedEvents = mutableListOf<String>()
        private val recordedEventDetails = mutableListOf<Pair<String, Map<String, Any>>>()

        override fun recordEvent(event: String) {
            recordedEvents += event
        }

        override fun recordEvent(event: String, details: Map<String, Any>) {
            recordedEvents += event
            recordedEventDetails += event to details
        }

        fun recordedEventDetailsFor(event: String): List<Map<String, Any>> {
            return recordedEventDetails.filter { it.first == event }.map { it.second }
        }

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
        override fun recordEntryAdded(numEntries: Int) = Unit
        override fun recordView(viewName: String) = Unit
        override fun optOutOfAnalytics() = Unit
        override fun optIntoAnalytics() = Unit
    }

    private class TestPresentlySettings : PresentlySettings {
        var notificationsEnabledValue = false
        var notificationTimeValue: LocalTime = LocalTime.parse("21:00")
        var reminderOnboardingSeenValue = false
        var notificationPermissionRequestedValue = false

        override fun getCurrentTheme(): String = "original"
        override fun setTheme(themeName: String) = Unit
        override fun isBiometricsEnabled(): Boolean = false
        override fun shouldLockApp(): Boolean = false
        override fun setOnPauseTime() = Unit
        override fun getFirstDayOfWeek(): Int = Calendar.MONDAY
        override fun shouldShowQuote(): Boolean = true
        override fun getAutomaticBackupCadence(): BackupCadence = BackupCadence.DAILY
        override fun getLocale(): String = "en-US"
        override fun hasEnabledNotifications(): Boolean = notificationsEnabledValue
        override fun setNotificationsEnabled(enabled: Boolean) {
            notificationsEnabledValue = enabled
        }
        override fun getNotificationTime(): LocalTime = notificationTimeValue
        override fun setNotificationTime(time: LocalTime) {
            notificationTimeValue = time
        }
        override fun hasUserDisabledAlarmReminders(context: Context): Boolean = false
        override fun hasSeenReminderOnboarding(): Boolean = reminderOnboardingSeenValue
        override fun markReminderOnboardingSeen() {
            reminderOnboardingSeenValue = true
        }
        override fun clearReminderOnboardingSeen() {
            reminderOnboardingSeenValue = false
        }
        override fun hasRequestedNotificationPermission(): Boolean = notificationPermissionRequestedValue
        override fun markNotificationPermissionRequested() {
            notificationPermissionRequestedValue = true
        }
        override fun getLinesPerEntryInTimeline(): Int = 10
        override fun shouldShowDayOfWeekInTimeline(): Boolean = false
        override fun getAccessToken(): DbxCredential? = null
        override fun setAccessToken(newToken: DbxCredential) = Unit
        override fun wasDropboxAuthInitiated(): Boolean = false
        override fun markDropboxAuthAsCancelled() = Unit
        override fun markDropboxAuthInitiated() = Unit
        override fun clearAccessToken() = Unit
        override fun isOptedIntoAnalytics(): Boolean = true
    }
}
