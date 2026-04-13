package journal.gratitude.com.gratitudejournal.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dropbox.core.oauth.DbxCredential
import dagger.hilt.android.lifecycle.HiltViewModel
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.model.CsvUriCreated
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.settings.BackupFrequency
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.settings.BackupProvider
import journal.gratitude.com.gratitudejournal.util.backups.BackupRestoreManager
import journal.gratitude.com.gratitudejournal.util.backups.BackupWorkScheduler
import journal.gratitude.com.gratitudejournal.util.backups.LocalExporter
import journal.gratitude.com.gratitudejournal.util.backups.RealCsvParser
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupFile
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class BackupSettingsViewModel @Inject constructor(
    private val repository: EntryRepository,
    private val backupPreferences: BackupPreferences,
    private val backupWorkScheduler: BackupWorkScheduler,
    private val backupRestoreManager: BackupRestoreManager,
    private val dropboxUploader: DropboxUploader,
    private val googleDriveBackupProvider: GoogleDriveBackupProvider,
    private val analytics: AnalyticsLogger,
    private val crashReporter: CrashReporter
) : ViewModel() {

    private val _state = MutableStateFlow(BackupSettingsUiState())
    val state: StateFlow<BackupSettingsUiState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<BackupSettingsEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effects: SharedFlow<BackupSettingsEffect> = _effects.asSharedFlow()

    private var pendingRestoreBackupFile: GoogleDriveBackupFile? = null
    private var screenViewRecorded = false

    init {
        refreshState()
        hydrateDropboxEmailIfNeeded()
    }

    fun onScreenShown() {
        if (screenViewRecorded) return
        screenViewRecorded = true
        analytics.recordView("BackupSettingsFragment")
    }

    fun onDropboxFrequencySelected(frequency: BackupFrequency) {
        updateFrequency(BackupProvider.DROPBOX, frequency)
    }

    fun onGoogleDriveFrequencySelected(frequency: BackupFrequency) {
        updateFrequency(BackupProvider.GOOGLE_DRIVE, frequency)
    }

    fun onImportClicked() {
        _state.update { it.copy(showImportWarning = true) }
    }

    fun onImportDismissed() {
        _state.update { it.copy(showImportWarning = false) }
    }

    fun onImportConfirmed() {
        _state.update { it.copy(showImportWarning = false) }
        _effects.tryEmit(BackupSettingsEffect.OpenImportPicker)
    }

    fun onExportClicked() {
        _effects.tryEmit(
            BackupSettingsEffect.OpenExportPicker(
                suggestedFileName = "PresentlyBackup${Instant.now().toString().replace(':', '-')}.csv"
            )
        )
    }

    fun onDropboxAuthenticated(token: DbxCredential) {
        viewModelScope.launch {
            backupPreferences.setDropboxCredential(token, null)
            val accountEmail = dropboxUploader.fetchCurrentAccountEmail()
            if (accountEmail != null) {
                backupPreferences.setDropboxAccountEmail(accountEmail)
            }
            backupWorkScheduler.schedule(
                BackupProvider.DROPBOX,
                backupPreferences.getDropboxState().frequency
            )
            refreshState()
        }
    }

    fun onDropboxAuthenticationCancelled() {
        backupPreferences.markDropboxAuthCancelled()
        refreshState()
        _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.dropbox_auth_failed))
    }

    fun onDisconnectDropboxClicked() {
        viewModelScope.launch {
            dropboxUploader.disconnect()
            refreshState()
        }
    }

    fun onGoogleDriveSignedIn(accountEmail: String) {
        viewModelScope.launch {
            backupPreferences.setGoogleDriveConnection(accountEmail)
            backupWorkScheduler.schedule(
                BackupProvider.GOOGLE_DRIVE,
                backupPreferences.getGoogleDriveState().frequency
            )
            refreshState(isCheckingGoogleRestore = true)

            runCatching {
                backupRestoreManager.loadGoogleDriveRestorePreview(accountEmail)
            }.onSuccess { preview ->
                pendingRestoreBackupFile = preview?.backupFile
                _state.update { current ->
                    current.copy(
                        googleDrive = backupPreferences.getGoogleDriveState().toUiState(),
                        isCheckingGoogleRestore = false,
                        restoreOffer = preview?.toUiState()
                    )
                }
            }.onFailure { throwable ->
                crashReporter.logHandledException(Exception(throwable))
                pendingRestoreBackupFile = null
                refreshState(isCheckingGoogleRestore = false)
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.google_drive_auth_failed))
            }
        }
    }

    fun onGoogleDriveSignInFailed(exception: Exception?) {
        if (exception != null) {
            crashReporter.logHandledException(exception)
        }
        _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.google_drive_auth_failed))
    }

    fun onDisconnectGoogleDriveClicked() {
        viewModelScope.launch {
            googleDriveBackupProvider.disconnect()
            pendingRestoreBackupFile = null
            refreshState(restoreOffer = null)
        }
    }

    fun onRestoreDismissed() {
        pendingRestoreBackupFile = null
        _state.update { it.copy(restoreOffer = null) }
    }

    fun onRestoreConfirmed() {
        val backupFile = pendingRestoreBackupFile ?: return
        viewModelScope.launch {
            runCatching {
                backupRestoreManager.restoreFromGoogleDrive(backupFile)
            }.onSuccess {
                pendingRestoreBackupFile = null
                _state.update { current -> current.copy(restoreOffer = null) }
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.restore_success))
            }.onFailure { throwable ->
                crashReporter.logHandledException(Exception(throwable))
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.error_parsing))
            }
        }
    }

    fun importFromCsv(csvBytes: ByteArray) {
        viewModelScope.launch {
            runCatching {
                val parser = CSVParser.parse(
                    ByteArrayInputStream(csvBytes),
                    StandardCharsets.UTF_8,
                    CSVFormat.DEFAULT
                )
                val entries = LocalExporter.convertCsvToEntries(RealCsvParser(parser))
                repository.addEntries(entries)
            }.onSuccess {
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.import_success))
            }.onFailure { throwable ->
                crashReporter.logHandledException(Exception(throwable))
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.error_parsing))
            }
        }
    }

    fun exportToCsv(uri: Uri, context: android.content.Context) {
        viewModelScope.launch {
            runCatching {
                LocalExporter.exportEntriesToCsvFile(
                    context = context,
                    source = uri,
                    items = repository.getEntries()
                )
            }.onSuccess { result ->
                if (result is CsvUriCreated) {
                    _effects.tryEmit(BackupSettingsEffect.ExportSucceeded(result.uri))
                } else {
                    _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.error_creating_csv_file))
                }
            }.onFailure { throwable ->
                crashReporter.logHandledException(Exception(throwable))
                _effects.tryEmit(BackupSettingsEffect.ShowMessage(R.string.error_creating_csv_file))
            }
        }
    }

    private fun updateFrequency(provider: BackupProvider, frequency: BackupFrequency) {
        viewModelScope.launch {
            backupPreferences.updateFrequency(provider, frequency)
            backupWorkScheduler.schedule(provider, frequency)
            analytics.recordSelectEvent(frequency.storageValue, "${provider.name.lowercase()}_backup_frequency")
            refreshState()
        }
    }

    private fun hydrateDropboxEmailIfNeeded() {
        viewModelScope.launch {
            val state = backupPreferences.getDropboxState()
            if (state.isConnected && state.accountEmail.isNullOrEmpty()) {
                val accountEmail = dropboxUploader.fetchCurrentAccountEmail()
                if (accountEmail != null) {
                    backupPreferences.setDropboxAccountEmail(accountEmail)
                    refreshState()
                }
            }
        }
    }

    private fun refreshState(
        restoreOffer: RestoreOfferUiState? = _state.value.restoreOffer,
        isCheckingGoogleRestore: Boolean = _state.value.isCheckingGoogleRestore
    ) {
        _state.value = BackupSettingsUiState(
            dropbox = backupPreferences.getDropboxState().toUiState(),
            googleDrive = backupPreferences.getGoogleDriveState().toUiState(),
            restoreOffer = restoreOffer,
            showImportWarning = _state.value.showImportWarning,
            isCheckingGoogleRestore = isCheckingGoogleRestore
        )
    }

    private fun journal.gratitude.com.gratitudejournal.settings.BackupProviderState.toUiState():
        BackupProviderUiState {
        return BackupProviderUiState(
            isConnected = isConnected,
            accountEmail = accountEmail,
            lastBackupTimestamp = lastBackupTimestamp,
            frequency = frequency
        )
    }

    private fun journal.gratitude.com.gratitudejournal.util.backups.RestorePreview.toUiState():
        RestoreOfferUiState {
        return RestoreOfferUiState(
            backupDateTimestamp = runCatching {
                Instant.parse(backupFile.modifiedTime).toEpochMilli()
            }.getOrNull(),
            entryCount = backupFile.entries.size,
            existingEntryCount = existingEntryCount
        )
    }
}

data class BackupSettingsUiState(
    val dropbox: BackupProviderUiState = BackupProviderUiState(),
    val googleDrive: BackupProviderUiState = BackupProviderUiState(),
    val restoreOffer: RestoreOfferUiState? = null,
    val showImportWarning: Boolean = false,
    val isCheckingGoogleRestore: Boolean = false
)

data class BackupProviderUiState(
    val isConnected: Boolean = false,
    val accountEmail: String? = null,
    val lastBackupTimestamp: Long? = null,
    val frequency: BackupFrequency = BackupFrequency.DAILY
)

data class RestoreOfferUiState(
    val backupDateTimestamp: Long?,
    val entryCount: Int,
    val existingEntryCount: Int
)

sealed interface BackupSettingsEffect {
    data class ShowMessage(val messageRes: Int) : BackupSettingsEffect
    data class OpenExportPicker(val suggestedFileName: String) : BackupSettingsEffect
    data object OpenImportPicker : BackupSettingsEffect
    data class ExportSucceeded(val uri: Uri) : BackupSettingsEffect
}
