package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun NotificationTroubleshooterScreen(
    state: StateFlow<NotificationTroubleshooterState>,
    onClose: () -> Unit,
    onFixItClicked: (NotificationTroubleshooterCheck) -> Unit,
    onContactSupportClicked: () -> Unit
) {
    val uiState by state.collectAsStateWithLifecycle()
    NotificationTroubleshooterScreenContent(
        state = uiState,
        onClose = onClose,
        onFixItClicked = onFixItClicked,
        onContactSupportClicked = onContactSupportClicked
    )
}

@Composable
fun NotificationTroubleshooterScreenContent(
    state: NotificationTroubleshooterState,
    onClose: () -> Unit,
    onFixItClicked: (NotificationTroubleshooterCheck) -> Unit,
    onContactSupportClicked: () -> Unit
) {
    val tokens = LocalPresentlyTheme.current

    //todo switch to an app toolbar
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = tokens.timelineBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("notification_troubleshooter_root")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.notification_troubleshooter_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = tokens.timelineHeader
                )
                TextButton(onClick = onClose) {
                    Text(text = stringResource(R.string.close))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading) {
                LoadingState()
                return@Column
            }

            state.checks.forEach { result ->
                CheckResultCard(
                    result = result,
                    onFixItClicked = onFixItClicked
                )
            }

            if (state.allChecksPass) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = tokens.timelineBackground
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.notification_troubleshooter_all_good),
                            style = MaterialTheme.typography.bodyLarge,
                            color = tokens.timelineBody
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onContactSupportClicked,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(text = stringResource(R.string.contact_support))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
    val tokens = LocalPresentlyTheme.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.notification_troubleshooter_loading),
            style = MaterialTheme.typography.bodyLarge,
            color = tokens.timelineBody
        )
    }
}

@Composable
private fun CheckResultCard(
    result: NotificationTroubleshooterCheckResult,
    onFixItClicked: (NotificationTroubleshooterCheck) -> Unit
) {
    val tokens = LocalPresentlyTheme.current
    val statusColor = if (result.passed) tokens.timelineHint else tokens.timelineHeader

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = tokens.timelineBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("check_${result.check.analyticsValue}")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result.check.title(),
                    style = MaterialTheme.typography.titleMedium,
                    color = statusColor
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(if (result.passed) R.drawable.ic_check else R.drawable.ic_close_24),
                        tint = statusColor,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = if (result.passed) {
                            stringResource(R.string.status_passed)
                        } else {
                            stringResource(R.string.status_failed)
                        },
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = statusColor
                    )
                }
            }

            if (!result.passed) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = result.check.failureExplanation(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = tokens.timelineBody
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onFixItClicked(result.check) },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(text = stringResource(R.string.fix_it))
                }
            }
        }
    }
}

@Composable
private fun NotificationTroubleshooterCheck.title(): String = when (this) {
    NotificationTroubleshooterCheck.POST_NOTIFICATIONS -> {
        stringResource(R.string.troubleshooter_check_notification_permission)
    }
    NotificationTroubleshooterCheck.APP_NOTIFICATIONS -> {
        stringResource(R.string.troubleshooter_check_app_notifications)
    }
    NotificationTroubleshooterCheck.EXACT_ALARM -> {
        stringResource(R.string.troubleshooter_check_exact_alarm)
    }
    NotificationTroubleshooterCheck.BATTERY_OPTIMIZATION -> {
        stringResource(R.string.troubleshooter_check_battery_optimization)
    }
}

@Composable
private fun NotificationTroubleshooterCheck.failureExplanation(): String = when (this) {
    NotificationTroubleshooterCheck.POST_NOTIFICATIONS -> {
        stringResource(R.string.troubleshooter_failure_notification_permission)
    }
    NotificationTroubleshooterCheck.APP_NOTIFICATIONS -> {
        stringResource(R.string.troubleshooter_failure_app_notifications)
    }
    NotificationTroubleshooterCheck.EXACT_ALARM -> {
        stringResource(R.string.troubleshooter_failure_exact_alarm)
    }
    NotificationTroubleshooterCheck.BATTERY_OPTIMIZATION -> {
        stringResource(R.string.troubleshooter_failure_battery_optimization)
    }
}
