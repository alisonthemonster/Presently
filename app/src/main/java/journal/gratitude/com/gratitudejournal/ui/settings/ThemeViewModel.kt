package journal.gratitude.com.gratitudejournal.ui.settings

import androidx.lifecycle.ViewModel
import com.presently.settings.PresentlySettings
import com.presently.ui.PresentlyColors
import com.presently.ui.toPresentlyColors
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val settings: PresentlySettings
): ViewModel() {

    fun getSelectedTheme(): PresentlyColors {
        return settings.getCurrentTheme().toPresentlyColors()
    }

    fun onThemeSelected(theme: String) {
        settings.setTheme(theme)
    }
}