package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import journal.gratitude.com.gratitudejournal.ui.themes.ThemeFragment

@Module
abstract class ThemeModule {

    @ContributesAndroidInjector
    internal abstract fun themeFragment(): ThemeFragment

}