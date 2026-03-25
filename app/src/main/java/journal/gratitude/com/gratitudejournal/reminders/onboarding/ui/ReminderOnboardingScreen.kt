package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import kotlinx.coroutines.flow.StateFlow
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderOnboardingScreen(
    state: StateFlow<ReminderOnboardingState>,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSkipForNow: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit
) {
    val uiState = state.collectAsStateWithLifecycle()
    ReminderOnboardingScreenContent(
        state = uiState.value,
        onDismiss = onDismiss,
        onPrimaryAction = onPrimaryAction,
        onSkipForNow = onSkipForNow,
        onTimeChanged = onTimeChanged
    )
}

@Composable
fun ReminderOnboardingScreenContent(
    state: ReminderOnboardingState,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit,
    onSkipForNow: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit
) {
    val tokens = LocalPresentlyTheme.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminder_onboarding_root"),
        color = tokens.timelineBackground
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            StepBackgroundLogos(
                step = state.currentStep,
                screenWidth = maxWidth,
                screenHeight = maxHeight
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 20.dp)
                    .padding(bottom = 152.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("reminder_onboarding_close")
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = "Dismiss onboarding"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                when (state.currentStep) {
                    ReminderOnboardingStep.TIME -> TimeStep(
                        selectedTime = state.selectedTime,
                        onTimeChanged = onTimeChanged
                    )

                    ReminderOnboardingStep.NOTIFICATIONS -> PermissionStep(
                        title = if (state.notificationPermissionDenied) {
                            stringResource(R.string.notification_permission_is_required_for_reminders)
                        } else {
                            stringResource(R.string.turn_on_notifications)
                        },
                        body = if (state.notificationPermissionDenied) {
                            stringResource(R.string.presently_can_t_send_reminders_without_your_permission_please_try_again)
                        } else {
                            ""
                        },
                        modifier = Modifier.testTag("notification_step")
                    )

                    ReminderOnboardingStep.EXACT_ALARM -> PermissionStep(
                        title = if (state.exactAlarmPermissionDenied) {
                            "Exact alarms are still blocked"
                        } else {
                            "Allow exact alarms"
                        },
                        body = if (state.exactAlarmPermissionDenied) {
                            stringResource(R.string.without_granting_exact_alarm_permissions_notifications_may_be_delayed_or_skipped_by_your_phone)
                        } else {
                            stringResource(R.string.presently_needs_exact_alarm_scheduling_to_send_notifications_at_the_right_time)
                        },
                        modifier = Modifier.testTag("exact_alarm_step")
                    )

                    ReminderOnboardingStep.SUCCESS -> SuccessStep(
                        selectedTime = state.selectedTime
                    )
                }
            }

            BottomActions(
                state = state,
                onPrimaryAction = onPrimaryAction,
                onSkipForNow = onSkipForNow,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BoxScope.StepBackgroundLogos(
    step: ReminderOnboardingStep,
    screenWidth: androidx.compose.ui.unit.Dp,
    screenHeight: androidx.compose.ui.unit.Dp
) {
    val tokens = LocalPresentlyTheme.current
    val logoColor = tokens.timelineBody.copy(alpha = 0.5f)
    val largeLogoSize = screenWidth * 1.35f
    val logoVerticalSpacing = screenHeight * 0.24f
    val bottomLogoY = screenHeight * 0.65f
    val secondLogoY = bottomLogoY - logoVerticalSpacing
    val sideLogoX = screenWidth * 0.52f
    val bottomLeftX = -(screenWidth * 0.25f)
    val topLogoX = screenWidth * 0.5f
    val topLogoY = -(screenHeight * 0.32f)

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()
    ) {
        BackgroundLogo(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = bottomLeftX, y = bottomLogoY)
                .size(largeLogoSize),
            color = logoColor
        )
    }

    AnimatedVisibility(
        visible = step >= ReminderOnboardingStep.NOTIFICATIONS,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
    ) {
        BackgroundLogo(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = sideLogoX, y = secondLogoY)
                .size(largeLogoSize)
                .graphicsLayer { scaleX = -1f },
            color = logoColor
        )
    }

    AnimatedVisibility(
        visible = step >= ReminderOnboardingStep.EXACT_ALARM,
        enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn()
    ) {
        BackgroundLogo(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = topLogoX, y = topLogoY)
                .size(largeLogoSize)
                .graphicsLayer { scaleY = -1f },
            color = logoColor
        )
    }
}

@Composable
private fun BackgroundLogo(
    modifier: Modifier,
    color: Color
) {
    Image(
        painter = painterResource(R.drawable.presently_leaves),
        contentDescription = null,
        colorFilter = ColorFilter.tint(color),
        modifier = modifier
    )
}

@Composable
private fun TimeStep(
    selectedTime: LocalTime,
    onTimeChanged: (LocalTime) -> Unit
) {
    val tokens = LocalPresentlyTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("time_step")
    ) {
        Text(
            text = stringResource(R.string.set_a_reminder_time),
            style = ReminderOnboardingTypography.Headline,
            color = tokens.timelineHeader
        )
        Spacer(modifier = Modifier.height(36.dp))
        ReminderTimePicker(
            selectedTime = selectedTime,
            onTimeChanged = onTimeChanged
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionStep(
    title: String,
    body: String,
    modifier: Modifier = Modifier
) {
    val tokens = LocalPresentlyTheme.current
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = ReminderOnboardingTypography.Headline.copy(fontWeight = FontWeight.SemiBold),
            color = tokens.timelineHeader
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = body,
            style = ReminderOnboardingTypography.Body,
            color = tokens.timelineBody.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun SuccessStep(
    selectedTime: LocalTime
) {
    val tokens = LocalPresentlyTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("success_step"),
        horizontalAlignment = Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(tokens.highlight, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Reminders are ready",
            style = ReminderOnboardingTypography.Headline.copy(fontWeight = FontWeight.SemiBold),
            color = tokens.timelineHeader
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = selectedTime.formatReminderTime(),
            style = ReminderOnboardingTypography.Accent.copy(fontWeight = FontWeight.Medium),
            color = tokens.timelineBody
        )
    }
}

@Composable
private fun BottomActions(
    state: ReminderOnboardingState,
    onPrimaryAction: () -> Unit,
    onSkipForNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tokens = LocalPresentlyTheme.current
    val primaryLabel = when (state.currentStep) {
        ReminderOnboardingStep.TIME -> "Continue"
        ReminderOnboardingStep.NOTIFICATIONS -> {
            if (state.notificationPermissionDenied) "Open settings" else "Allow notifications"
        }

        ReminderOnboardingStep.EXACT_ALARM -> "Open settings"
        ReminderOnboardingStep.SUCCESS -> "Done"
    }
    val showSkipForNow = when (state.currentStep) {
        ReminderOnboardingStep.NOTIFICATIONS -> state.notificationPermissionDenied
        ReminderOnboardingStep.EXACT_ALARM -> state.exactAlarmPermissionDenied
        else -> false
    }

    Column(
        modifier = modifier
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 32.dp, vertical = 20.dp)
    ) {
        if (showSkipForNow) {
            OutlinedButton(
                onClick = onSkipForNow,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skip_for_now"),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = tokens.timelineBody
                ),
                shape = RoundedCornerShape(0.dp)
            ) {
                Text(stringResource(R.string.skip_for_now), style = ReminderOnboardingTypography.Button)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = onPrimaryAction,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("primary_cta"),
            colors = ButtonDefaults.buttonColors(
                containerColor = tokens.fab,
                contentColor = tokens.fabText
            ),
            shape = RoundedCornerShape(0.dp)
        ) {
            Text(primaryLabel, style = ReminderOnboardingTypography.Button)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimePicker(
    selectedTime: LocalTime,
    onTimeChanged: (LocalTime) -> Unit
) {
    val tokens = LocalPresentlyTheme.current
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime.hour,
        initialMinute = selectedTime.minute,
        is24Hour = false
    )

    LaunchedEffect(selectedTime.hour, selectedTime.minute) {
        if (timePickerState.hour != selectedTime.hour) {
            timePickerState.hour = selectedTime.hour
        }
        if (timePickerState.minute != selectedTime.minute) {
            timePickerState.minute = selectedTime.minute
        }
    }

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        val updatedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
        if (updatedTime != selectedTime) {
            onTimeChanged(updatedTime)
        }
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        TimePicker(
            state = timePickerState,
            modifier = Modifier.testTag("material_time_picker"),
            colors = TimePickerDefaults.colors(
                clockDialColor = tokens.entryBody.copy(alpha = 0.94f),
                clockDialSelectedContentColor = tokens.entryBody,
                clockDialUnselectedContentColor = tokens.timelineBackground.copy(alpha = 0.92f),
                selectorColor = tokens.timelineBackground,
                containerColor = Color.Transparent,
                periodSelectorBorderColor = tokens.timelineBody.copy(alpha = 0.14f),
                periodSelectorSelectedContainerColor = tokens.fab,
                periodSelectorUnselectedContainerColor = tokens.entryBackground.copy(alpha = 0.96f),
                periodSelectorSelectedContentColor = tokens.fabText,
                periodSelectorUnselectedContentColor = tokens.entryBody,
                timeSelectorSelectedContainerColor = tokens.entryBackground.copy(alpha = 0.96f),
                timeSelectorUnselectedContainerColor = tokens.entryBackground.copy(alpha = 0.72f),
                timeSelectorSelectedContentColor = tokens.fab,
                timeSelectorUnselectedContentColor = tokens.entryBody
            )
        )
    }
}

private fun LocalTime.formatReminderTime(): String {
    return format(
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    )
}

private object ReminderOnboardingTypography {
    val Headline = TextStyle(
        fontFamily = PresentlyFontFamilies.accent,
        fontSize = 32.sp,
        lineHeight = 36.sp
    )

    val Accent = TextStyle(
        fontFamily = PresentlyFontFamilies.accent,
        fontSize = 22.sp,
        lineHeight = 28.sp
    )

    val Body = TextStyle(
        fontFamily = PresentlyFontFamilies.body,
        fontSize = 18.sp,
        lineHeight = 26.sp
    )

    val Button = TextStyle(
        fontFamily = PresentlyFontFamilies.body,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    )
}
