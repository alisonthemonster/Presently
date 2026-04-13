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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
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
    onRestoreDismissed: () -> Unit,
    onRestoreConfirmed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.backup_and_restore)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.back)
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
                iconRes = R.drawable.ic_google_drive,
                connectLabel = stringResource(R.string.sign_in_with_google),
                disconnectLabel = stringResource(R.string.disconnect),
                notConnectedLabel = stringResource(R.string.not_connected),
                onConnectClick = onGoogleDriveConnectClick,
                onDisconnectClick = onGoogleDriveDisconnectClick,
                onFrequencySelected = onGoogleDriveFrequencySelected
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.manual_backup_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onExportClick
                    ) {
                        Text(stringResource(R.string.one_time_export))
                    }
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onImportClick
                    ) {
                        Text(stringResource(R.string.import_entries_from_backup))
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
            onDismissRequest = onRestoreDismissed
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (restoreOffer.existingEntryCount > 0) {
                    Text(
                        text = stringResource(
                            R.string.google_drive_restore_merge_warning,
                            restoreOffer.existingEntryCount
                        )
                    )
                }
                if (state.isCheckingGoogleRestore) {
                    Text(stringResource(R.string.checking_for_backup))
                }
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onRestoreConfirmed
                ) {
                    Text(stringResource(R.string.restore))
                }
                TextButton(
                    modifier = Modifier.align(Alignment.End),
                    onClick = onRestoreDismissed
                ) {
                    Text(stringResource(R.string.skip))
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
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
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
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Text(
                    text = stringResource(R.string.backup_frequency),
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BackupFrequency.entries.forEach { frequency ->
                        FilterChip(
                            selected = state.frequency == frequency,
                            onClick = { onFrequencySelected(frequency) },
                            label = { Text(stringResource(frequency.labelRes)) }
                        )
                    }
                }

                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onDisconnectClick
                ) {
                    Text(disconnectLabel)
                }
            } else {
                Text(
                    text = notConnectedLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onConnectClick
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
    return DateFormat.getMediumDateFormat(androidx.compose.ui.platform.LocalContext.current)
        .format(Date(timestamp)) + " " +
        DateFormat.getTimeFormat(androidx.compose.ui.platform.LocalContext.current)
            .format(Date(timestamp))
}
