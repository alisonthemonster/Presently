package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import journal.gratitude.com.gratitudejournal.util.reminders.ReminderReceiver

@Module
abstract class ReminderModule {

    @ContributesAndroidInjector
    internal abstract fun reminderReceiver(): ReminderReceiver
}