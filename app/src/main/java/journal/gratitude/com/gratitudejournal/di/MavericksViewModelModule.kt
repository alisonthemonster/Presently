package journal.gratitude.com.gratitudejournal.di

import com.presently.mavericks_utils.AssistedViewModelFactory
import com.presently.mavericks_utils.MavericksViewModelComponent
import com.presently.mavericks_utils.MavericksViewModelKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.multibindings.IntoMap
import journal.gratitude.com.gratitudejournal.ui.entry.EntryViewModel
import journal.gratitude.com.gratitudejournal.ui.entry_viewpager.EntryViewPagerViewModel

@Module
@InstallIn(MavericksViewModelComponent::class)
interface MavericksViewModelModule {
    @Binds
    @IntoMap
    @MavericksViewModelKey(EntryViewModel::class)
    fun entryViewModelFactory(factory: EntryViewModel.Factory): AssistedViewModelFactory<*, *>

    @Binds
    @IntoMap
    @MavericksViewModelKey(EntryViewPagerViewModel::class)
    fun entryViewPagerViewModelFactory(factory: EntryViewPagerViewModel.Factory): AssistedViewModelFactory<*, *>
}