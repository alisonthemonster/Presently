package journal.gratitude.com.gratitudejournal.ui.settings

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.ui.theme.LocalPresentlyTheme
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSettingsScreen(
    state: BackupSettingsUiState,
    onBackClick: () -> Unit,
    onDropboxConnectClick: () -> Unit,
    onDropboxDisconnectClick: () -> Unit,
    onDropboxFrequencySelected: (BackupFrequency) -> Unit,
    onGoogleDriveConnectClick: () -> Unit,
    onGoogleDriveDisconnectClick: () -> Unit,
    onGoogleDriveFrequencySelected: (BackupFrequency) -> Unit,
    onImportClick: () -> Unit,
    onImportDismissed: () -> Unit,
    onImportConfirmed: () -> Unit,
    onExportClick: () -> Unit,
    onBackupGuideClick: () -> Unit,
    onRestoreDismissed: () -> Unit,
    onRestoreConfirmed: () -> Unit
) {
    val theme = LocalPresentlyTheme.current

    Scaffold(
        containerColor = theme.timelineHeader,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = theme.toolbar,
                    titleContentColor = theme.toolbarItem,
                    navigationIconContentColor = theme.toolbarItem
                ),
                title = {
                    Text(
                        stringResource(R.string.backup_and_restore),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back),
                            tint = theme.toolbarItem
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BackupProviderCard(
                title = stringResource(R.string.dropbox),
                state = state.dropbox,
                iconRes = R.drawable.ic_dropbox,
                connectLabel = stringResource(R.string.connect_dropbox),
                disconnectLabel = stringResource(R.string.disconnect),
                notConnectedLabel = stringResource(R.string.not_connected),
                onConnectClick = onDropboxConnectClick,
                onDisconnectClick = onDropboxDisconnectClick,
                onFrequencySelected = onDropboxFrequencySelected
            )

            BackupProviderCard(
                title = stringResource(R.string.google_drive),
                state = state.googleDrive,
                iconRes = R.drawable.ic_drive,
                connectLabel = stringResource(R.string.sign_in_with_google),
                disconnectLabel = stringResource(R.string.disconnect),
                notConnectedLabel = stringResource(R.string.not_connected),
                onConnectClick = onGoogleDriveConnectClick,
                onDisconnectClick = onGoogleDriveDisconnectClick,
                onFrequencySelected = onGoogleDriveFrequencySelected
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.entryBackground,
                    contentColor = theme.entryBody
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.manual_backup_section),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.entryBody,
                            contentColor = theme.entryBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.one_time_export))
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.entryBody,
                            contentColor = theme.entryBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        onClick = onImportClick
                    ) {
                        Text(stringResource(R.string.import_entries_from_backup))
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = theme.entryBackground,
                    contentColor = theme.entryBody
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.learn_more),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = stringResource(R.string.backup_and_restore_summary),
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.entryHint
                    )
                    Button(
                        onClick = onBackupGuideClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = theme.entryBody,
                            contentColor = theme.entryBackground
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.learn_more))
                    }
                }
            }
        }
    }

    if (state.showImportWarning) {
        AlertDialog(
            onDismissRequest = onImportDismissed,
            title = { Text(stringResource(R.string.import_data_dialog)) },
            text = { Text(stringResource(R.string.import_data_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onImportConfirmed) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = onImportDismissed) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    state.restoreOffer?.let { restoreOffer ->
        ModalBottomSheet(
            onDismissRequest = onRestoreDismissed,
            containerColor = theme.entryBackground,
            contentColor = theme.entryBody,
            scrimColor = theme.toolbar.copy(alpha = 0.42f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.google_drive_restore_found,
                        formatTimestamp(restoreOffer.backupDateTimestamp),
                        restoreOffer.entryCount
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (restoreOffer.existingEntryCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.google_drive_restore_merge_warning,
                            restoreOffer.existingEntryCount
                        ),
                        color = theme.entryHint
                    )
                }
                if (state.isCheckingGoogleRestore) {
                    Text(
                        stringResource(R.string.checking_for_backup),
                        color = theme.entryHint
                    )
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.entryBody,
                        contentColor = theme.entryBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = onRestoreConfirmed
                ) {
                    Text(stringResource(R.string.restore))
                }
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestoreDismissed
                ) {
                    Text(
                        stringResource(R.string.skip),
                        color = theme.entryBody
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BackupProviderCard(
    title: String,
    state: BackupProviderUiState,
    iconRes: Int,
    connectLabel: String,
    disconnectLabel: String,
    notConnectedLabel: String,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onFrequencySelected: (BackupFrequency) -> Unit
) {
    val theme = LocalPresentlyTheme.current
    val disabledAlpha = 0.45f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = theme.timelineBackground,
            contentColor = theme.timelineBody
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = theme.entryHeader
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (state.isConnected) {
                state.accountEmail?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (state.lastBackupTimestamp != null) {
                    Text(
                        text = stringResource(
                            R.string.last_backup_label,
                            formatTimestamp(state.lastBackupTimestamp)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = theme.timelineHint
                    )
                }

                Text(
                    text = stringResource(R.string.backup_frequency),
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.timelineHint
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackupFrequency.entries.forEach { frequency ->
                        FilterChip(
                            selected = state.frequency == frequency,
                            onClick = { onFrequencySelected(frequency) },
                            label = { Text(stringResource(frequency.labelRes)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = theme.highlight,
                                selectedLabelColor = theme.timelineBackground
                            )
                        )
                    }
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDisconnectClick,
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(disconnectLabel)
                }
            } else {
                Text(
                    text = notConnectedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = theme.timelineHint
                )
                Text(
                    text = stringResource(R.string.backup_frequency),
                    style = MaterialTheme.typography.labelLarge,
                    color = theme.timelineHint,
                    modifier = Modifier.alpha(disabledAlpha)
                )

                Button(
                    onClick = onConnectClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.entryBody,
                        contentColor = theme.entryBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(connectLabel)
                }

            }
        }
    }
}

private val BackupFrequency.labelRes: Int
    get() = when (this) {
        BackupFrequency.DAILY -> R.string.daily
        BackupFrequency.MONTHLY -> R.string.monthly
        BackupFrequency.ON_EVERY_CHANGE -> R.string.every
    }

@Composable
private fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return stringResource(R.string.unknown_backup_date)
    return DateFormat.getMediumDateFormat(LocalContext.current)
        .format(Date(timestamp)) + " " +
            DateFormat.getTimeFormat(LocalContext.current)
                .format(Date(timestamp))
}
