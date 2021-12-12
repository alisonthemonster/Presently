package com.presently.sharing.di

import com.presently.mavericks_utils.AssistedViewModelFactory
import com.presently.mavericks_utils.MavericksViewModelComponent
import com.presently.mavericks_utils.MavericksViewModelKey
import com.presently.sharing.view.SharingViewModel
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