package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_ADVANCED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_COMPLETED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_DISMISSED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PERMISSION_DENIED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_PERMISSION_GRANTED
import journal.gratitude.com.gratitudejournal.logging.REMINDER_ONBOARDING_VIEWED
import journal.gratitude.com.gratitudejournal.reminders.onboarding.data.ReminderOnboardingRepository
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.BuildReminderOnboardingStepsUseCase
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderPermissionSnapshot
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.threeten.bp.LocalTime
import javax.inject.Inject

@HiltViewModel
class ReminderOnboardingViewModel @Inject constructor(
    private val repository: ReminderOnboardingRepository,
    private val analytics: AnalyticsLogger,
    private val buildReminderOnboardingSteps: BuildReminderOnboardingStepsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        ReminderOnboardingState(selectedTime = repository.getNotificationTime())
    )
    val state: StateFlow<ReminderOnboardingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<ReminderOnboardingEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<ReminderOnboardingEffect> = _effects.asSharedFlow()

    private lateinit var permissionSnapshot: ReminderPermissionSnapshot
    private var completed = false

    fun start(snapshot: ReminderPermissionSnapshot) {
        permissionSnapshot = snapshot
        if (_state.value.isStarted) {
            refreshSteps()
            return
        }

        repository.markReminderOnboardingSeen()
        analytics.recordEvent(REMINDER_ONBOARDING_VIEWED)

        _state.update {
            it.copy(
                steps = buildReminderOnboardingSteps(snapshot),
                isStarted = true
            )
        }
    }

    fun onTimeChanged(time: LocalTime) {
        _state.update { it.copy(selectedTime = time) }
    }

    fun onTimeSaved() {
        val state = _state.value
        repository.setNotificationTime(state.selectedTime)
        moveFromTimeStep()
    }

    fun onPrimaryActionClicked() {
        when (_state.value.currentStep) {
            ReminderOnboardingStep.TIME -> onTimeSaved()
            ReminderOnboardingStep.NOTIFICATIONS -> {
                if (
                    _state.value.notificationPermissionDenied ||
                    !permissionSnapshot.canRequestNotificationPermission
                ) {
                    _effects.tryEmit(ReminderOnboardingEffect.OpenNotificationSettings)
                } else {
                    _effects.tryEmit(ReminderOnboardingEffect.RequestNotificationPermission)
                }
            }
            ReminderOnboardingStep.EXACT_ALARM -> {
                _effects.tryEmit(ReminderOnboardingEffect.OpenExactAlarmSettings)
            }
            ReminderOnboardingStep.SUCCESS -> onDoneClicked()
        }
    }

    fun onNotificationPermissionDialogResult(granted: Boolean) {
        if (granted) {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_GRANTED)
            permissionSnapshot = permissionSnapshot.copy(
                notificationsEnabled = true,
                canRequestNotificationPermission = false
            )
            _state.update { it.copy(notificationPermissionDenied = false) }
            advanceFromNotificationStep()
        } else {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_DENIED)
            _state.update { it.copy(notificationPermissionDenied = true) }
        }
    }

    fun onNotificationSettingsResult(enabled: Boolean) {
        permissionSnapshot = permissionSnapshot.copy(notificationsEnabled = enabled)
        if (enabled) {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_GRANTED)
            _state.update { it.copy(notificationPermissionDenied = false) }
            advanceFromNotificationStep()
        } else {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_DENIED)
            _state.update { it.copy(notificationPermissionDenied = true) }
            refreshSteps()
        }
    }

    fun onExactAlarmSettingsResult(granted: Boolean) {
        permissionSnapshot = permissionSnapshot.copy(exactAlarmGranted = granted)
        if (granted) {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_GRANTED)
            _state.update { it.copy(exactAlarmPermissionDenied = false) }
            showSuccess()
        } else {
            analytics.recordEvent(REMINDER_ONBOARDING_PERMISSION_DENIED)
            _state.update { it.copy(exactAlarmPermissionDenied = true) }
            refreshSteps()
        }
    }

    fun onSkipForNowClicked() {
        if (_state.value.currentStep == ReminderOnboardingStep.EXACT_ALARM) {
            showSuccess()
            return
        }
        analytics.recordEvent(REMINDER_ONBOARDING_DISMISSED)
        _effects.tryEmit(ReminderOnboardingEffect.Dismiss)
    }

    fun onDismissRequested() {
        analytics.recordEvent(REMINDER_ONBOARDING_DISMISSED)
        _effects.tryEmit(ReminderOnboardingEffect.Dismiss)
    }

    fun onDoneClicked() {
        _effects.tryEmit(ReminderOnboardingEffect.Dismiss)
    }

    private fun moveFromTimeStep() {
        refreshSteps()
        val nextStep = _state.value.steps.getOrNull(_state.value.currentStepIndex + 1)
            ?: ReminderOnboardingStep.SUCCESS

        if (nextStep == ReminderOnboardingStep.SUCCESS) {
            showSuccess()
        } else {
            moveToStep(nextStep)
        }
    }

    private fun advanceFromNotificationStep() {
        refreshSteps()
        val steps = _state.value.steps
        val nextStep = when {
            steps.contains(ReminderOnboardingStep.EXACT_ALARM) -> ReminderOnboardingStep.EXACT_ALARM
            else -> ReminderOnboardingStep.SUCCESS
        }

        if (nextStep == ReminderOnboardingStep.SUCCESS) {
            showSuccess()
        } else {
            moveToStep(nextStep)
        }
    }

    private fun showSuccess() {
        if (!completed) {
            repository.enableAndScheduleReminders()
            analytics.recordEvent(REMINDER_ONBOARDING_COMPLETED)
            completed = true
        }
        refreshSteps()
        moveToStep(ReminderOnboardingStep.SUCCESS)
    }

    private fun refreshSteps() {
        _state.update {
            val steps = buildReminderOnboardingSteps(permissionSnapshot)
            val fallbackStep = when {
                steps.contains(it.currentStep) -> it.currentStep
                steps.contains(ReminderOnboardingStep.EXACT_ALARM) -> ReminderOnboardingStep.EXACT_ALARM
                else -> ReminderOnboardingStep.SUCCESS
            }
            it.copy(steps = steps, currentStep = fallbackStep)
        }
    }

    private fun moveToStep(step: ReminderOnboardingStep) {
        val current = _state.value.currentStep
        if (current != step) {
            analytics.recordEvent(REMINDER_ONBOARDING_ADVANCED)
        }
        _state.update { it.copy(currentStep = step) }
    }
}
