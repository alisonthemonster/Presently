package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import journal.gratitude.com.gratitudejournal.ui.security.AppLockFragment

@Module
abstract class AppLockModule {

    @ContributesAndroidInjector
    internal abstract fun appLockFragment(): AppLockFragment

}