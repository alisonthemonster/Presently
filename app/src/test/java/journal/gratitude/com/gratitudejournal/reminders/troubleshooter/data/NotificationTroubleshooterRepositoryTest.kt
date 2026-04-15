package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data

import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui.NotificationTroubleshooterCheck
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
class NotificationTroubleshooterRepositoryTest {

    @Test
    @Config(sdk = [30])
    fun runChecks_omitsExactAlarmBelowAndroid12() {
        val repository = NotificationTroubleshooterRepository(
            ApplicationProvider.getApplicationContext()
        )

        val checks = repository.runChecks().map { it.check }

        assertThat(checks).doesNotContain(NotificationTroubleshooterCheck.EXACT_ALARM)
    }

    @Test
    @Config(sdk = [31])
    fun runChecks_includesExactAlarmOnAndroid12AndAbove() {
        val repository = NotificationTroubleshooterRepository(
            ApplicationProvider.getApplicationContext()
        )

        val checks = repository.runChecks().map { it.check }

        assertThat(checks).contains(NotificationTroubleshooterCheck.EXACT_ALARM)
    }
}
