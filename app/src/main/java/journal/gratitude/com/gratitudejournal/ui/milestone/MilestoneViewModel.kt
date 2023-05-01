package journal.gratitude.com.gratitudejournal.ui.milestone

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.presently.logging.AnalyticsLogger
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.model.CLICKED_SHARE_MILESTONE
import journal.gratitude.com.gratitudejournal.navigation.MilestoneArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class MilestoneViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analytics: AnalyticsLogger,
    private val settings: PresentlySettings,
) : ViewModel() {
    private val navArgs = MilestoneArgs(savedStateHandle)

    private val _state = MutableStateFlow(MilestoneViewState())
    val state: StateFlow<MilestoneViewState> = _state

    init {
        Log.d("blerg", "navArgs.milestoneNumber: ${navArgs.milestoneNumber}")
        _state.value = _state.value.copy(milestoneNumber = navArgs.milestoneNumber)
    }

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun onShareClicked() {
        analytics.recordEvent(CLICKED_SHARE_MILESTONE)
    }
}
