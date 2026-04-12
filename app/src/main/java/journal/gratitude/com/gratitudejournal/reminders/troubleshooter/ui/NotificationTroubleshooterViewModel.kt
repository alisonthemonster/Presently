package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.coroutine_utils.AppCoroutineDispatchers
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.NOTIFICATION_TROUBLESHOOTER_CHECK_FAILED
import journal.gratitude.com.gratitudejournal.logging.NOTIFICATION_TROUBLESHOOTER_CONTACT_SUPPORT_TAPPED
import journal.gratitude.com.gratitudejournal.logging.NOTIFICATION_TROUBLESHOOTER_FIX_IT_TAPPED
import journal.gratitude.com.gratitudejournal.logging.NOTIFICATION_TROUBLESHOOTER_OPENED
import journal.gratitude.com.gratitudejournal.reminders.troubleshooter.data.NotificationTroubleshooterRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NotificationTroubleshooterViewModel @Inject constructor(
    private val repository: NotificationTroubleshooterRepository,
    private val analytics: AnalyticsLogger,
    private val dispatchers: AppCoroutineDispatchers
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationTroubleshooterState())
    val state: StateFlow<NotificationTroubleshooterState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<NotificationTroubleshooterEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<NotificationTroubleshooterEffect> = _effects.asSharedFlow()

    private var hasRecordedOpened = false

    fun loadChecks() {
        viewModelScope.launch {
            if (!hasRecordedOpened) {
                analytics.recordEvent(NOTIFICATION_TROUBLESHOOTER_OPENED)
                hasRecordedOpened = true
            }

            _state.update { it.copy(isLoading = true) }
            val checks = withContext(dispatchers.io) {
                repository.runChecks()
            }
            _state.update { it.copy(isLoading = false, checks = checks) }
            checks.filterNot(NotificationTroubleshooterCheckResult::passed).forEach { result ->
                analytics.recordEvent(
                    NOTIFICATION_TROUBLESHOOTER_CHECK_FAILED,
                    mapOf(CHECK_KEY to result.check.analyticsValue)
                )
            }
        }
    }

    fun onFixItClicked(check: NotificationTroubleshooterCheck) {
        analytics.recordEvent(
            NOTIFICATION_TROUBLESHOOTER_FIX_IT_TAPPED,
            mapOf(CHECK_KEY to check.analyticsValue)
        )
        when (check) {
            NotificationTroubleshooterCheck.POST_NOTIFICATIONS,
            NotificationTroubleshooterCheck.APP_NOTIFICATIONS,
            NotificationTroubleshooterCheck.EXACT_ALARM -> {
                _effects.tryEmit(NotificationTroubleshooterEffect.OpenExactAlarmSettings)
            }
            NotificationTroubleshooterCheck.BATTERY_OPTIMIZATION -> {
                _effects.tryEmit(NotificationTroubleshooterEffect.OpenBatteryOptimizationSettings)
            }
        }
    }

    fun onContactSupportClicked() {
        analytics.recordEvent(NOTIFICATION_TROUBLESHOOTER_CONTACT_SUPPORT_TAPPED)
        _effects.tryEmit(
            NotificationTroubleshooterEffect.ContactSupport(
                repository.buildSupportEmailData()
            )
        )
    }

    private companion object {
        private const val CHECK_KEY = "check"
    }
}
