package journal.gratitude.com.gratitudejournal.util.reminders

import android.app.AlarmManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.threeten.bp.LocalTime
import java.util.Calendar
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class NotificationSchedulerTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val originalTimeZone = TimeZone.getDefault()
    private val zone = TimeZone.getTimeZone("America/Edmonton")
    private var now = 0L
    private val scheduler = NotificationScheduler(currentTimeMillis = { now })

    @Before
    fun setUp() {
        TimeZone.setDefault(zone)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `alarm before today's reminder time is scheduled for today`() {
        setNow(hour = 20, minute = 0)

        scheduler.setNotificationTime(context, LocalTime.of(21, 0))

        val triggerAt = nextAlarmTime()
        assertThat(triggerAt).isEqualTo(timeAt(now, daysFromNow = 0, hour = 21, minute = 0))
    }

    @Test
    fun `alarm after today's reminder time is scheduled for tomorrow`() {
        setNow(hour = 21, minute = 30)

        scheduler.setNotificationTime(context, LocalTime.of(21, 0))

        val triggerAt = nextAlarmTime()
        assertThat(triggerAt).isEqualTo(timeAt(now, daysFromNow = 1, hour = 21, minute = 0))
    }

    @Test
    fun `rescheduling right after the alarm fires schedules tomorrow, not a loop`() {
        // the receiver reschedules a moment after 9pm; this must never land in the past
        setNow(hour = 21, minute = 0, second = 0, millis = 500)

        scheduler.setNotificationTime(context, LocalTime.of(21, 0))

        val triggerAt = nextAlarmTime()
        assertThat(triggerAt).isGreaterThan(now)
        assertThat(triggerAt).isEqualTo(timeAt(now, daysFromNow = 1, hour = 21, minute = 0))
    }

    @Test
    fun `rescheduling exactly at the reminder time schedules tomorrow`() {
        setNow(hour = 21, minute = 0)

        scheduler.setNotificationTime(context, LocalTime.of(21, 0))

        assertThat(nextAlarmTime()).isEqualTo(timeAt(now, daysFromNow = 1, hour = 21, minute = 0))
    }

    @Test
    fun `alarm fires at the start of the minute regardless of when it was scheduled`() {
        setNow(hour = 8, minute = 15, second = 42, millis = 123)

        scheduler.setNotificationTime(context, LocalTime.of(21, 0))

        val cal = Calendar.getInstance().apply { timeInMillis = nextAlarmTime() }
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(0)
        assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(0)
    }

    @Test
    fun `alarm is always scheduled in the future for any time of day`() {
        for (hour in 0..23) {
            setNow(hour = hour, minute = 0, second = 30)

            scheduler.setNotificationTime(context, LocalTime.of(21, 0))

            assertThat(nextAlarmTime()).isGreaterThan(now)
        }
    }

    private fun nextAlarmTime(): Long = shadowOf(alarmManager).nextScheduledAlarm!!.triggerAtMs

    private fun setNow(hour: Int, minute: Int, second: Int = 0, millis: Int = 0) {
        now = Calendar.getInstance(zone).apply {
            clear()
            set(2026, Calendar.NOVEMBER, 4, hour, minute, second)
            set(Calendar.MILLISECOND, millis)
        }.timeInMillis
    }

    private fun timeAt(base: Long, daysFromNow: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance(zone).apply {
            timeInMillis = base
            add(Calendar.DATE, daysFromNow)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}
