package journal.gratitude.com.gratitudejournal.di

import androidx.lifecycle.ViewModel
import dagger.Binds
import dagger.Module
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap
import journal.gratitude.com.gratitudejournal.ui.entry.EntryFragment
import journal.gratitude.com.gratitudejournal.ui.entry.EntryViewModel

@Module
abstract class EntryModule {

    @ContributesAndroidInjector(modules = [
        ViewModelBuilder::class
    ])
    internal abstract fun entryFragment(): EntryFragment

    @Binds
    @IntoMap
    @ViewModelKey(EntryViewModel::class)
    abstract fun bindViewModel(viewModel: EntryViewModel): ViewModel
}