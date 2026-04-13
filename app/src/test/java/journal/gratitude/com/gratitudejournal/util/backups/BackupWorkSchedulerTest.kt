package journal.gratitude.com.gratitudejournal.util.backups

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
class BackupWorkSchedulerTest {

    private lateinit var scheduler: BackupWorkScheduler

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val backupPreferences = BackupPreferences(
            PreferenceManager.getDefaultSharedPreferences(context),
            object : AnalyticsLogger {
                override fun recordEvent(event: String) = Unit
                override fun recordEvent(event: String, details: Map<String, Any>) = Unit
                override fun recordSelectEvent(selectedContent: String, selectedContentType: String) = Unit
                override fun recordEntryAdded(numEntries: Int) = Unit
                override fun recordView(viewName: String) = Unit
                override fun optOutOfAnalytics() = Unit
                override fun optIntoAnalytics() = Unit
            }
        )
        scheduler = BackupWorkScheduler(context, backupPreferences)
    }

    @Test
    fun scheduleConfigFor_dailyUsesOneDayInterval() {
        val config = scheduler.scheduleConfigFor(BackupFrequency.DAILY)

        assertThat(config?.repeatInterval).isEqualTo(1L)
        assertThat(config?.timeUnit).isEqualTo(TimeUnit.DAYS)
    }

    @Test
    fun scheduleConfigFor_monthlyUsesThirtyDayInterval() {
        val config = scheduler.scheduleConfigFor(BackupFrequency.MONTHLY)

        assertThat(config?.repeatInterval).isEqualTo(30L)
        assertThat(config?.timeUnit).isEqualTo(TimeUnit.DAYS)
    }

    @Test
    fun scheduleConfigFor_onEveryChangeDoesNotCreatePeriodicSchedule() {
        assertThat(scheduler.scheduleConfigFor(BackupFrequency.ON_EVERY_CHANGE)).isNull()
    }
}
