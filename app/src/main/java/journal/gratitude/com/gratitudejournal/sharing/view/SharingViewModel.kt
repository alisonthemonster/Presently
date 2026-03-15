package journal.gratitude.com.gratitudejournal.sharing.view

import com.airbnb.mvrx.MavericksViewModel
import com.airbnb.mvrx.MavericksViewModelFactory
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.mavericks_utils.AssistedViewModelFactory
import journal.gratitude.com.gratitudejournal.mavericks_utils.hiltMavericksViewModelFactory
import journal.gratitude.com.gratitudejournal.sharing.data.SharingViewDesign
import journal.gratitude.com.gratitudejournal.sharing.data.SharingViewState
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject

class SharingViewModel @AssistedInject constructor(
    @Assisted initialState: SharingViewState,
    private val analyticsLogger: AnalyticsLogger
) : MavericksViewModel<SharingViewState>(initialState) {

    fun onCreate() {
        analyticsLogger.recordEvent("viewedShareScreen")
    }

    fun selectColorScheme(newDesign: SharingViewDesign) {
        setState {
            copy(viewDesign = newDesign)
        }
    }

    fun clickFinish() {
        analyticsLogger.recordEvent("sharedImage")
        setState {
            copy(clicksShare = true)
        }
    }

    fun sharingComplete() {
        setState {
            copy(clicksShare = false)
        }
    }

    @AssistedFactory
    interface Factory : AssistedViewModelFactory<SharingViewModel, SharingViewState> {
        override fun create(state: SharingViewState): SharingViewModel
    }

    companion object : MavericksViewModelFactory<SharingViewModel, SharingViewState> by hiltMavericksViewModelFactory(
        SharingViewModel::class.java
    )

}
