package journal.gratitude.com.gratitudejournal.ui

import android.content.Context
import android.content.SharedPreferences
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.settings.wiring.PresentlySettingsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.preference.PreferenceManager
import org.threeten.bp.LocalTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PresentlySettings binding to use in tests.
 *
 * Hilt will inject a [FakePresentlySettings] instead of a [RealPresentlySettings].
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PresentlySettingsModule::class]
)
abstract class FakeSettingsModule {
    @Singleton
    @Binds
    abstract fun bindSettings(repo: FakePresentlySettings): PresentlySettings

    companion object {
        @Provides
        fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return PreferenceManager.getDefaultSharedPreferences(context)
        }
    }
}

@Singleton
class FakePresentlySettings @Inject constructor(): PresentlySettings {
    var notificationsEnabledValue = false
    var notificationTimeValue: LocalTime = LocalTime.parse("21:00")
    var reminderOnboardingSeenValue = false
    var notificationPermissionRequestedValue = false

    override fun getCurrentTheme(): String {
        return "Original"
    }

    override fun setTheme(themeName: String) {}

    override fun isBiometricsEnabled(): Boolean {
        return false
    }

    override fun shouldLockApp(): Boolean {
        return false
    }

    override fun setOnPauseTime() {}

    override fun getFirstDayOfWeek(): Int {
        return Calendar.MONDAY
    }

    override fun shouldShowQuote(): Boolean {
        return true
    }

    override fun getLocale(): String {
        return "en-US"
    }

    override fun hasEnabledNotifications(): Boolean {
        return notificationsEnabledValue
    }

    override fun setNotificationsEnabled(enabled: Boolean) {
        notificationsEnabledValue = enabled
    }

    override fun getNotificationTime(): LocalTime {
        return notificationTimeValue
    }

    override fun setNotificationTime(time: LocalTime) {
        notificationTimeValue = time
    }

    override fun hasUserDisabledAlarmReminders(context: Context): Boolean {
        return false
    }

    override fun hasSeenReminderOnboarding(): Boolean {
        return reminderOnboardingSeenValue
    }

    override fun markReminderOnboardingSeen() {
        reminderOnboardingSeenValue = true
    }

    override fun clearReminderOnboardingSeen() {
        reminderOnboardingSeenValue = false
    }

    override fun hasRequestedNotificationPermission(): Boolean {
        return notificationPermissionRequestedValue
    }

    override fun markNotificationPermissionRequested() {
        notificationPermissionRequestedValue = true
    }

    override fun getLinesPerEntryInTimeline(): Int {
        return 10
    }

    override fun shouldShowDayOfWeekInTimeline(): Boolean {
        return false
    }

    override fun isOptedIntoAnalytics(): Boolean = true
}
