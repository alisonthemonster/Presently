package journal.gratitude.com.gratitudejournal.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import journal.gratitude.com.gratitudejournal.ui.dialog.CelebrateDialogFragment

@Module
abstract class CelebrateDialogModule {

    @ContributesAndroidInjector
    internal abstract fun celebrateDialogFragment(): CelebrateDialogFragment

}