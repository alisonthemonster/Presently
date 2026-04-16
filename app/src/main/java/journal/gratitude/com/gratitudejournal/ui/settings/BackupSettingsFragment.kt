package journal.gratitude.com.gratitudejournal.ui.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dropbox.core.android.Auth
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.settings.BackupPreferences
import journal.gratitude.com.gratitudejournal.ui.setStatusBarColorsForBackground
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.util.backups.google.GoogleDriveBackupProvider
import journal.gratitude.com.gratitudejournal.util.backups.dropbox.DropboxUploader
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class BackupSettingsFragment : Fragment() {

    private val viewModel: BackupSettingsViewModel by viewModels()

    @Inject lateinit var backupPreferences: BackupPreferences
    @Inject lateinit var crashReporter: CrashReporter
    @Inject lateinit var googleDriveBackupProvider: GoogleDriveBackupProvider

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "googleSignInLauncher: resultCode=${result.resultCode} (RESULT_OK=${Activity.RESULT_OK})")
            if (result.resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "googleSignInLauncher: sign-in activity returned non-OK result, user may have cancelled")
                viewModel.onGoogleDriveSignInFailed(null)
                return@registerForActivityResult
            }

            handleGoogleSignInResult(result.data)
        }

    private val readCsvResultContract =
        registerForActivityResult(OpenCsvDocumentContract()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(context, R.string.file_not_csv, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            try {
                requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                    viewModel.importFromCsv(inputStream.readBytes())
                } ?: run {
                    Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
                }
            } catch (exception: Exception) {
                crashReporter.logHandledException(exception)
                Toast.makeText(context, R.string.error_parsing, Toast.LENGTH_SHORT).show()
            }
        }

    private val saveCsvResultContract =
        registerForActivityResult(CreateCsvDocumentContract()) { uri: Uri? ->
            if (uri == null) {
                Toast.makeText(context, R.string.error_creating_csv_file, Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }
            viewModel.exportToCsv(uri, requireContext())
        }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                PresentlyTheme {
                    val state = viewModel.state.collectAsStateWithLifecycle()
                    BackupSettingsScreen(
                        state = state.value,
                        onBackClick = { parentFragmentManager.popBackStack() },
                        onDropboxConnectClick = {
                            DropboxUploader.authorizeDropboxAccess(
                                requireContext(),
                                backupPreferences
                            )
                        },
                        onDropboxDisconnectClick = viewModel::onDisconnectDropboxClicked,
                        onDropboxFrequencySelected = viewModel::onDropboxFrequencySelected,
                        onGoogleDriveConnectClick = {
                            startGoogleDriveAuthorization()
                        },
                        onGoogleDriveDisconnectClick = viewModel::onDisconnectGoogleDriveClicked,
                        onGoogleDriveFrequencySelected = viewModel::onGoogleDriveFrequencySelected,
                        onImportClick = viewModel::onImportClicked,
                        onImportDismissed = viewModel::onImportDismissed,
                        onImportConfirmed = viewModel::onImportConfirmed,
                        onExportClick = viewModel::onExportClicked,
                        onBackupGuideClick = ::openBackupGuide,
                        onRestoreDismissed = viewModel::onRestoreDismissed,
                        onRestoreConfirmed = viewModel::onRestoreConfirmed
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.onScreenShown()
        prepareWindow()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is BackupSettingsEffect.ShowMessage ->
                            Toast.makeText(context, effect.messageRes, Toast.LENGTH_SHORT).show()
                        is BackupSettingsEffect.OpenExportPicker ->
                            saveCsvResultContract.launch(effect.suggestedFileName)
                        BackupSettingsEffect.OpenImportPicker ->
                            readCsvResultContract.launch(OpenCsvDocumentContract.mimeTypes)
                        is BackupSettingsEffect.ExportSucceeded -> showExportSuccess(effect.uri)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (backupPreferences.wasDropboxAuthInitiated()) {
            val token = Auth.getDbxCredential()
            if (token == null) {
                viewModel.onDropboxAuthenticationCancelled()
            } else {
                viewModel.onDropboxAuthenticated(token)
            }
        }
    }

    private fun prepareWindow() {
        val window = requireActivity().window
        window.statusBarColor = Color.TRANSPARENT
        val typedValue = TypedValue()
        requireActivity().theme.resolveAttribute(R.attr.timelineBackgroundColor, typedValue, true)
        setStatusBarColorsForBackground(window, typedValue.data)
    }

    private fun showExportSuccess(uri: Uri) {
        com.google.android.material.snackbar.Snackbar.make(
            requireView(),
            R.string.export_success,
            com.google.android.material.snackbar.Snackbar.LENGTH_LONG
        ).setAction(R.string.open) {
            try {
                startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "text/csv")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                )
            } catch (exception: ActivityNotFoundException) {
                crashReporter.logHandledException(exception)
                Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
            }
        }.show()
    }

    private fun openBackupGuide() {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(BACKUP_GUIDE_URL))
            )
        } catch (exception: ActivityNotFoundException) {
            crashReporter.logHandledException(exception)
            Toast.makeText(context, R.string.no_app_found, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGoogleDriveAuthorization() {
        Log.d(TAG, "startGoogleDriveAuthorization: launching Google Sign-In")
        val signInIntent = googleDriveBackupProvider.getSignInIntent()
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(data: Intent?) {
        Log.d(TAG, "handleGoogleSignInResult: processing Google Sign-In result")
        viewLifecycleOwner.lifecycleScope.launch {
            val accountEmail = googleDriveBackupProvider.handleSignInResult(data)

            if (accountEmail.isNullOrBlank()) {
                Log.e(TAG, "handleGoogleSignInResult: account email is null/blank")
                viewModel.onGoogleDriveSignInFailed(
                    IllegalStateException("Google Sign-In succeeded but account email was unavailable")
                )
            } else {
                Log.d(TAG, "handleGoogleSignInResult: Google Drive sign-in succeeded for account: $accountEmail")
                viewModel.onGoogleDriveSignedIn(accountEmail)
            }
        }
    }

    companion object {
        private const val TAG = "BackupSettings"
        private const val BACKUP_GUIDE_URL = "https://presently-app.firebaseapp.com/export.html"
    }
}
