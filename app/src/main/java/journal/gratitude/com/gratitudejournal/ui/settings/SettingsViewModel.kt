package journal.gratitude.com.gratitudejournal.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.presently.logging.CrashReporter
import com.presently.settings.PresentlySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: PresentlySettings,
    private val crashReporter: CrashReporter,
) : ViewModel() {

    fun onAppBackgrounded() {
        settings.onAppBackgrounded()
    }

    fun onAuthenticationSucceeded() {
        viewModelScope.launch {
            settings.onAuthenticationSucceeded()
        }
    }

    fun shouldAppLock(): Boolean {
        return settings.shouldLockApp()
    }

    fun onUnknownAuthenticationError(errorCode: Int, errString: CharSequence) {
        crashReporter.logHandledException(Exception("Code: $errorCode: $errString"))
    }
}