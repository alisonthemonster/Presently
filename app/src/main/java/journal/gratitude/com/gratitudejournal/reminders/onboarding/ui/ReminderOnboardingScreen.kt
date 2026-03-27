package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyFontFamilies
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.delay
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
    onTimeChanged: (LocalTime) -> Unit,
    successAnimationProgressOverride: Float? = null
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

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 20.dp),
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(top = 72.dp, bottom = 152.dp),
                contentAlignment = Alignment.Center
            ) {
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
                            "Presently needs notification permissions in order to deliver daily reminders"
                        },
                        modifier = Modifier.testTag("notification_step")
                    )

                    ReminderOnboardingStep.EXACT_ALARM -> PermissionStep(
                        title = if (state.exactAlarmPermissionDenied) {
                            stringResource(R.string.exact_alarms_are_still_blocked)
                        } else {
                            stringResource(R.string.allow_exact_alarms)
                        },
                        body = if (state.exactAlarmPermissionDenied) {
                            stringResource(R.string.without_granting_exact_alarm_permissions_notifications_may_be_delayed_or_skipped_by_your_phone)
                        } else {
                            stringResource(R.string.presently_needs_exact_alarm_scheduling_to_send_notifications_at_the_right_time)
                        },
                        modifier = Modifier.testTag("exact_alarm_step")
                    )

                    ReminderOnboardingStep.SUCCESS -> SuccessStep(
                        selectedTime = state.selectedTime,
                        animationProgressOverride = successAnimationProgressOverride
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
    val entranceDurationMillis = 800
    val isSuccessStep = step == ReminderOnboardingStep.SUCCESS
    val tokens = LocalPresentlyTheme.current
    val logoColor = tokens.timelineBody.copy(alpha = 0.25f)
    val largeLogoSize = screenWidth * 1.35f
    val bottomLogoY = screenHeight * 0.24f
    val secondLogoY = screenHeight * 0.24f
    val secondLogoX = screenWidth * 0.58f
    val bottomLeftX = -(largeLogoSize * 0.33f) + (screenWidth * 0.15f)
    val topLogoX = screenWidth * 0.5f
    val topLogoY = -(screenHeight * 0.27f)
    val fourthLogoX = -(largeLogoSize * 0.34f)
    val fourthLogoY = -(largeLogoSize * 0.32f)
    var firstLogoTarget by remember { mutableFloatStateOf(0f) }
    var thirdLogoTarget by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        firstLogoTarget = 1f
        delay(120)
        thirdLogoTarget = 1f
    }
    val firstLogoProgress by animateFloatAsState(
        targetValue = if (isSuccessStep) 0f else firstLogoTarget,
        animationSpec = tween(
            durationMillis = entranceDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "firstLogoProgress"
    )
    val secondLogoProgress by animateFloatAsState(
        targetValue = if (isSuccessStep) {
            0f
        } else if (step >= ReminderOnboardingStep.NOTIFICATIONS) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = entranceDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "secondLogoProgress"
    )
    val thirdLogoProgress by animateFloatAsState(
        targetValue = if (isSuccessStep) 0f else thirdLogoTarget,
        animationSpec = tween(
            durationMillis = entranceDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "thirdLogoProgress"
    )
    val fourthLogoProgress by animateFloatAsState(
        targetValue = if (isSuccessStep) {
            0f
        } else if (step >= ReminderOnboardingStep.EXACT_ALARM) {
            1f
        } else {
            0f
        },
        animationSpec = tween(
            durationMillis = entranceDurationMillis,
            easing = FastOutSlowInEasing
        ),
        label = "fourthLogoProgress"
    )
    val density = LocalDensity.current
    val firstLogoTranslationY =
        with(density) { (1f - firstLogoProgress) * (screenHeight * 0.18f).toPx() }
    val secondLogoTranslationX =
        with(density) { ((1f - secondLogoProgress) * (screenWidth * 0.22f).toPx()) }
    val secondLogoBaseTranslationY = with(density) { -(screenHeight * 0.22f).toPx() }
    val secondLogoTranslationY = with(density) {
        secondLogoBaseTranslationY + ((1f - secondLogoProgress) * (screenHeight * 0.18f).toPx())
    }
    val thirdLogoTranslationY =
        with(density) { -((1f - thirdLogoProgress) * (screenHeight * 0.18f).toPx()) }
    val fourthLogoTranslationX =
        with(density) { -((1f - fourthLogoProgress) * (screenWidth * 0.22f).toPx()) }

    BackgroundLogo(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = bottomLeftX, y = bottomLogoY)
            .size(largeLogoSize)
            .graphicsLayer {
                translationY = firstLogoTranslationY
                alpha = firstLogoProgress
            },
        color = logoColor
    )

    BackgroundLogo(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = secondLogoX, y = secondLogoY)
            .size(largeLogoSize)
            .graphicsLayer {
                translationX = secondLogoTranslationX
                translationY = secondLogoTranslationY
                alpha = secondLogoProgress
                scaleX = -1f
                rotationZ = -90f
            },
        color = logoColor
    )

    BackgroundLogo(
        modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = topLogoX, y = topLogoY)
            .size(largeLogoSize)
            .graphicsLayer {
                translationY = thirdLogoTranslationY
                alpha = thirdLogoProgress
                scaleY = -1f
            },
        color = logoColor
    )

    BackgroundLogo(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(x = fourthLogoX, y = fourthLogoY)
            .size(largeLogoSize)
            .graphicsLayer {
                translationX = fourthLogoTranslationX
                alpha = fourthLogoProgress
                scaleX = -1f
                rotationZ = 105f
            },
        color = logoColor
    )
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
            .testTag("time_step"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.set_a_reminder_time),
            style = ReminderOnboardingTypography.Headline,
            color = tokens.timelineHeader,
            textAlign = TextAlign.Center
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = ReminderOnboardingTypography.Headline.copy(fontWeight = FontWeight.SemiBold),
            color = tokens.timelineHeader,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = body,
            style = ReminderOnboardingTypography.Body,
            color = tokens.timelineBody.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessStep(
    selectedTime: LocalTime,
    animationProgressOverride: Float? = null
) {
    val tokens = LocalPresentlyTheme.current
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.logo_animated)
    )
    val animatedProgress by animateLottieCompositionAsState(
        composition = composition,
    )
    val progress = animationProgressOverride ?: animatedProgress
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("success_step"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(224.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.reminder_success),
            style = ReminderOnboardingTypography.Headline.copy(fontWeight = FontWeight.SemiBold),
            color = tokens.timelineHeader,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = selectedTime.formatReminderTime(),
            color = tokens.timelineBody,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.see_you_tomorrow),
            color = tokens.timelineBody,
            textAlign = TextAlign.Center
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
        ReminderOnboardingStep.TIME -> stringResource(R.string.continue_to_exit)
        ReminderOnboardingStep.NOTIFICATIONS -> {
            if (state.notificationPermissionDenied) stringResource(R.string.open_settings) else stringResource(
                R.string.allow_notifications
            )
        }

        ReminderOnboardingStep.EXACT_ALARM -> stringResource(R.string.open_settings)
        ReminderOnboardingStep.SUCCESS -> stringResource(R.string.done)
    }
    val showSkipForNow = when (state.currentStep) {
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
                Text(
                    stringResource(R.string.skip_for_now),
                    style = ReminderOnboardingTypography.Button
                )
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
