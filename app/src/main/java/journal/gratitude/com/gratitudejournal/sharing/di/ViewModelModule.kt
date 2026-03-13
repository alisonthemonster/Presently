package journal.gratitude.com.gratitudejournal.sharing.di

import journal.gratitude.com.gratitudejournal.mavericks_utils.AssistedViewModelFactory
import journal.gratitude.com.gratitudejournal.mavericks_utils.MavericksViewModelComponent
import journal.gratitude.com.gratitudejournal.mavericks_utils.MavericksViewModelKey
import journal.gratitude.com.gratitudejournal.sharing.view.SharingViewModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap

@Module
@InstallIn(MavericksViewModelComponent::class)
interface ViewModelModule {
    @Binds
    @IntoMap
    @MavericksViewModelKey(SharingViewModel::class)
    fun sharingViewModelFactory(factory: SharingViewModel.Factory): AssistedViewModelFactory<*, *>
}