package journal.gratitude.com.gratitudejournal.reminders.onboarding.ui

import android.widget.NumberPicker
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.reminders.onboarding.domain.ReminderOnboardingStep
import kotlinx.coroutines.delay
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReminderOnboardingScreen(
    state: kotlinx.coroutines.flow.StateFlow<ReminderOnboardingState>,
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
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("reminder_onboarding_root"),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            BotanicalBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("reminder_onboarding_close")) {
                        Image(
                            painter = painterResource(R.drawable.ic_close_24),
                            contentDescription = "Dismiss onboarding"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredReveal(index = 0) {
                    Text(
                        text = "Daily reminders, set your way",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 40.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                StaggeredReveal(index = 1) {
                    Text(
                        text = "Choose a time, finish permissions, and Presently will take care of the rest.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.78f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                StepProgress(state = state)

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedContent(targetState = state.currentStep, label = "reminder_onboarding_step") { step ->
                    when (step) {
                        ReminderOnboardingStep.TIME -> TimeStep(
                            selectedTime = state.selectedTime,
                            onContinue = onPrimaryAction,
                            onTimeChanged = onTimeChanged
                        )
                        ReminderOnboardingStep.NOTIFICATIONS -> PermissionStep(
                            title = if (state.notificationPermissionDenied) {
                                "Notifications are still off"
                            } else {
                                "Turn on notifications"
                            },
                            body = if (state.notificationPermissionDenied) {
                                "Presently still can't send reminders. You can try again or open app settings to allow notifications."
                            } else {
                                "Allow notifications so your daily reminder can show up at the time you picked."
                            },
                            primaryLabel = if (state.notificationPermissionDenied) {
                                "Open settings"
                            } else {
                                "Allow notifications"
                            },
                            showSkipForNow = state.notificationPermissionDenied,
                            onPrimaryAction = onPrimaryAction,
                            onSkipForNow = onSkipForNow,
                            modifier = Modifier.testTag("notification_step")
                        )
                        ReminderOnboardingStep.EXACT_ALARM -> PermissionStep(
                            title = if (state.exactAlarmPermissionDenied) {
                                "Exact alarms are still blocked"
                            } else {
                                "Allow exact alarms"
                            },
                            body = if (state.exactAlarmPermissionDenied) {
                                "Presently still needs exact alarm access to fire your reminder on time. Open settings and allow it when you're ready."
                            } else {
                                "On this Android version, exact alarm access keeps your reminder from drifting. We'll take you straight to the app setting."
                            },
                            primaryLabel = "Open settings",
                            showSkipForNow = state.exactAlarmPermissionDenied,
                            onPrimaryAction = onPrimaryAction,
                            onSkipForNow = onSkipForNow,
                            modifier = Modifier.testTag("exact_alarm_step")
                        )
                        ReminderOnboardingStep.SUCCESS -> SuccessStep(
                            selectedTime = state.selectedTime,
                            onDone = onPrimaryAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.BotanicalBackground() {
    val transition = rememberInfiniteTransition(label = "botanical")
    val driftA by transition.animateFloat(
        initialValue = -8f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftA"
    )
    val driftB by transition.animateFloat(
        initialValue = 10f,
        targetValue = -12f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftB"
    )

    Image(
        painter = painterResource(R.drawable.ic_monstera),
        contentDescription = null,
        modifier = Modifier
            .size(240.dp)
            .offset(x = (-48).dp, y = (24 + driftA).dp)
            .alpha(0.08f)
    )

    Image(
        painter = painterResource(R.drawable.ic_tiny_flower),
        contentDescription = null,
        modifier = Modifier
            .size(120.dp)
            .align(Alignment.BottomEnd)
            .offset(x = (-12).dp, y = (-64 + driftB).dp)
            .alpha(0.16f)
    )
}

@Composable
private fun StepProgress(state: ReminderOnboardingState) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        state.steps.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .width(if (index == state.currentStepIndex) 36.dp else 18.dp)
                    .background(
                        color = if (index <= state.currentStepIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)
                        },
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun TimeStep(
    selectedTime: LocalTime,
    onContinue: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("time_step"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Pick your reminder time",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You can change this later, but choosing it now lets us finish setup in one pass.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            ReminderTimeWheelPicker(
                selectedTime = selectedTime,
                onTimeChanged = onTimeChanged
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Reminder time: ${selectedTime.formatReminderTime()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("primary_cta")
            ) {
                Text("Save reminder time")
            }
        }
    }
}

@Composable
private fun PermissionStep(
    title: String,
    body: String,
    primaryLabel: String,
    showSkipForNow: Boolean,
    onPrimaryAction: () -> Unit,
    onSkipForNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("primary_cta")
            ) {
                Text(primaryLabel)
            }
            if (showSkipForNow) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onSkipForNow,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("skip_for_now")
                ) {
                    Text("Skip for now")
                }
            }
        }
    }
}

@Composable
private fun SuccessStep(
    selectedTime: LocalTime,
    onDone: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("success_step"),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF4F0E6)
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFF2E7D5A), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_check),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Your reminders are ready",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Presently will check in every day at ${selectedTime.formatReminderTime()}.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("primary_cta")
            ) {
                Text("Done")
            }
        }
    }
}

@Composable
private fun ReminderTimeWheelPicker(
    selectedTime: LocalTime,
    onTimeChanged: (LocalTime) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            values = (1..12).map(Int::toString),
            selectedIndex = selectedTime.to12Hour() - 1
        ) { hour ->
            onTimeChanged(selectedTime.withHour(hour.to24Hour(selectedTime.hour < 12)))
        }
        Text(":", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(horizontal = 8.dp))
        WheelPicker(
            values = (0..59).map { it.toString().padStart(2, '0') },
            selectedIndex = selectedTime.minute
        ) { minute ->
            onTimeChanged(selectedTime.withMinute(minute))
        }
        Spacer(modifier = Modifier.width(8.dp))
        WheelPicker(
            values = listOf("AM", "PM"),
            selectedIndex = if (selectedTime.hour < 12) 0 else 1
        ) { index ->
            onTimeChanged(selectedTime.withHour(selectedTime.to12Hour().to24Hour(index == 0)))
        }
    }
}

@Composable
private fun WheelPicker(
    values: List<String>,
    selectedIndex: Int,
    onValueSelected: (Int) -> Unit
) {
    AndroidView(
        factory = { context ->
            NumberPicker(context).apply {
                minValue = 0
                maxValue = values.lastIndex
                displayedValues = values.toTypedArray()
                wrapSelectorWheel = true
                descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS
                setOnValueChangedListener { _, _, newVal ->
                    onValueSelected(if (values.size == 12) newVal + 1 else newVal)
                }
            }
        },
        update = { picker ->
            picker.minValue = 0
            picker.maxValue = values.lastIndex
            picker.displayedValues = null
            picker.displayedValues = values.toTypedArray()
            if (picker.value != selectedIndex) {
                picker.value = selectedIndex
            }
            picker.setOnValueChangedListener { _, _, newVal ->
                onValueSelected(if (values.size == 12) newVal + 1 else newVal)
            }
        },
        modifier = Modifier.size(width = 92.dp, height = 180.dp)
    )
}

@Composable
private fun StaggeredReveal(
    index: Int,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(index) {
        delay(index * 90L)
        visible = true
    }
    AnimatedVisibility(visible = visible) {
        content()
    }
}

private fun LocalTime.to12Hour(): Int {
    val hour = hour % 12
    return if (hour == 0) 12 else hour
}

private fun Int.to24Hour(isAm: Boolean): Int {
    return when {
        this == 12 && isAm -> 0
        this == 12 -> 12
        isAm -> this
        else -> this + 12
    }
}

private fun LocalTime.formatReminderTime(): String {
    return format(
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    )
}
