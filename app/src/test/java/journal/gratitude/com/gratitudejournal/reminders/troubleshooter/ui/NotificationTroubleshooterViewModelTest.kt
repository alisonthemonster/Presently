package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.dropbox.core.oauth.DbxCredential
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data.NotificationTroubleshooterRepository
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

        viewModel.onFixItClicked(NotificationTroubleshooterCheck.EXACT_ALARM)

        assertThat(effect.await())
            .isEqualTo(NotificationTroubleshooterEffect.OpenExactAlarmSettings)
        assertThat(
            analytics.recordedEventDetailsFor("notification_troubleshooter_fix_it_tapped")
        ).containsExactly(mapOf("check" to "exact_alarm"))
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
    ) : NotificationTroubleshooterRepository(context) {
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
