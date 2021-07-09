package journal.gratitude.com.gratitudejournal.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap
import journal.gratitude.com.gratitudejournal.ui.entry.EntryViewModel

@Module
@InstallIn(MavericksViewModelComponent::class)
interface MavericksViewModelModule {
    @Binds
    @IntoMap
    @MavericksViewModelKey(EntryViewModel::class)
    fun helloViewModelFactory(factory: EntryViewModel.Factory): AssistedViewModelFactory<*, *>
}