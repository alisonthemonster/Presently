package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data.NotificationTroubleshooterRepository
import journal.gratitude.com.gratitudejournal.settings.BackupCadence
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.testUtils.MainDispatcherRule
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.threeten.bp.LocalTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class NotificationTroubleshooterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var analytics: TestAnalyticsLogger
    private lateinit var repository: FakeNotificationTroubleshooterRepository
    private lateinit var viewModel: NotificationTroubleshooterViewModel

    @Before
    fun setUp() {
        analytics = TestAnalyticsLogger()
        repository = FakeNotificationTroubleshooterRepository(
            ApplicationProvider.getApplicationContext()
        )
        viewModel = NotificationTroubleshooterViewModel(
            repository = repository,
            analytics = analytics,
            dispatchers = AppCoroutineDispatchers(
                io = mainDispatcherRule.dispatcher,
                computation = mainDispatcherRule.dispatcher,
                main = mainDispatcherRule.dispatcher
            )
        )
    }

    @Test
    fun loadChecks_updatesState_andTracksFailures() = runTest {
        repository.results = listOf(
            NotificationTroubleshooterCheck.POST_NOTIFICATIONS.resultFor(true),
            NotificationTroubleshooterCheck.APP_NOTIFICATIONS.resultFor(false)
        )

        viewModel.loadChecks()
        runCurrent()

        assertThat(viewModel.state.value.isLoading).isFalse()
        assertThat(viewModel.state.value.checks).containsExactly(
            NotificationTroubleshooterCheck.POST_NOTIFICATIONS.resultFor(true),
            NotificationTroubleshooterCheck.APP_NOTIFICATIONS.resultFor(false)
        ).inOrder()
        assertThat(analytics.recordedEvents).contains("notification_troubleshooter_opened")
        assertThat(
            analytics.recordedEventDetailsFor("notification_troubleshooter_check_failed")
        ).containsExactly(mapOf("check" to "app_notifications"))
    }

    @Test
    fun onFixItClicked_tracksEvent_andEmitsExpectedEffect() = runTest {
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }

        viewModel.onFixItClicked(NotificationTroubleshooterCheck.BATTERY_OPTIMIZATION)

        assertThat(effect.await())
            .isEqualTo(NotificationTroubleshooterEffect.OpenBatteryOptimizationSettings)
        assertThat(
            analytics.recordedEventDetailsFor("notification_troubleshooter_fix_it_tapped")
        ).containsExactly(mapOf("check" to "battery_optimization"))
    }

    @Test
    fun onContactSupportClicked_tracksEvent_andEmitsSupportEmail() = runTest {
        val effect = async(start = CoroutineStart.UNDISPATCHED) { viewModel.effects.first() }

        viewModel.onContactSupportClicked()

        val emittedEffect = effect.await() as NotificationTroubleshooterEffect.ContactSupport
        assertThat(emittedEffect.emailData.subject)
            .isEqualTo("Presently notification troubleshooting")
        assertThat(emittedEffect.emailData.body).isEqualTo("body")
        assertThat(emittedEffect.emailData.recipients.toList())
            .containsExactly("support@example.com")
        assertThat(analytics.recordedEvents)
            .contains("notification_troubleshooter_contact_support_tapped")
    }

    private class FakeNotificationTroubleshooterRepository(
        context: Context
    ) : NotificationTroubleshooterRepository(context, FakeSettings()) {
        var results: List<NotificationTroubleshooterCheckResult> = emptyList()

        override fun runChecks(): List<NotificationTroubleshooterCheckResult> = results

        override fun buildSupportEmailData(): SupportEmailData {
            return SupportEmailData(
                recipients = arrayOf("support@example.com"),
                subject = "Presently notification troubleshooting",
                body = "body"
            )
        }
    }

    private class FakeSettings : PresentlySettings {
        override fun getCurrentTheme(): String = "original"
        override fun setTheme(themeName: String) = Unit
        override fun isBiometricsEnabled(): Boolean = false
        override fun shouldLockApp(): Boolean = false
        override fun setOnPauseTime() = Unit
        override fun getFirstDayOfWeek(): Int = 1
        override fun shouldShowQuote(): Boolean = true
        override fun getAutomaticBackupCadence(): BackupCadence = BackupCadence.DAILY
        override fun getLocale(): String = "en"
        override fun hasEnabledNotifications(): Boolean = false
        override fun setNotificationsEnabled(enabled: Boolean) = Unit
        override fun getNotificationTime(): LocalTime = LocalTime.NOON
        override fun setNotificationTime(time: LocalTime) = Unit
        override fun hasNotificationTimeConfigured(): Boolean = true
        override fun hasUserDisabledAlarmReminders(context: Context): Boolean = false
        override fun hasSeenReminderOnboarding(): Boolean = false
        override fun markReminderOnboardingSeen() = Unit
        override fun clearReminderOnboardingSeen() = Unit
        override fun hasRequestedNotificationPermission(): Boolean = false
        override fun markNotificationPermissionRequested() = Unit
        override fun getLinesPerEntryInTimeline(): Int = 3
        override fun shouldShowDayOfWeekInTimeline(): Boolean = false
        override fun getAccessToken(): DbxCredential? = null
        override fun setAccessToken(newToken: DbxCredential) = Unit
        override fun wasDropboxAuthInitiated(): Boolean = false
        override fun markDropboxAuthAsCancelled() = Unit
        override fun markDropboxAuthInitiated() = Unit
        override fun clearAccessToken() = Unit
        override fun isOptedIntoAnalytics(): Boolean = true
    }

    private class TestAnalyticsLogger : AnalyticsLogger {
        val recordedEvents = mutableListOf<String>()
        private val eventDetails = mutableMapOf<String, MutableList<Map<String, Any>>>()

        override fun recordEvent(event: String) {
            recordedEvents += event
        }

        override fun recordEvent(event: String, details: Map<String, Any>) {
            recordedEvents += event
            eventDetails.getOrPut(event) { mutableListOf() }.add(details)
        }

        override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
        override fun recordEntryAdded(numEntries: Int) = Unit
        override fun recordView(viewName: String) = Unit
        override fun optOutOfAnalytics() = Unit
        override fun optIntoAnalytics() = Unit

        fun recordedEventDetailsFor(event: String): List<Map<String, Any>> {
            return eventDetails[event].orEmpty()
        }
    }
}
