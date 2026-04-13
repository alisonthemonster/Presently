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

    private val googleAuthorizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            Log.d(TAG, "googleAuthorizationLauncher: resultCode=${result.resultCode} (RESULT_OK=${Activity.RESULT_OK})")
            if (result.resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "googleAuthorizationLauncher: authorization activity returned non-OK result, user may have cancelled or OAuth client is misconfigured")
                viewModel.onGoogleDriveSignInFailed(null)
                return@registerForActivityResult
            }

            handleGoogleAuthorizationResultIntent(result.data)
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

    private fun startGoogleDriveAuthorization() {
        Log.d(TAG, "startGoogleDriveAuthorization: beginning interactive authorization")
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                googleDriveBackupProvider.beginInteractiveAuthorization()
            }.onSuccess { authorizationResult ->
                Log.d(TAG, "beginInteractiveAuthorization succeeded: hasResolution=${authorizationResult.hasResolution()}, hasAccessToken=${authorizationResult.accessToken != null}")
                handleGoogleAuthorizationResult(authorizationResult)
            }.onFailure { throwable ->
                Log.e(TAG, "beginInteractiveAuthorization failed", throwable)
                viewModel.onGoogleDriveSignInFailed(
                    if (throwable is Exception) throwable else Exception(throwable)
                )
            }
        }
    }

    private fun handleGoogleAuthorizationResultIntent(data: Intent?) {
        Log.d(TAG, "handleGoogleAuthorizationResultIntent: data=${data != null}")
        runCatching {
            googleDriveBackupProvider.getAuthorizationResultFromIntent(data)
        }.onSuccess { authorizationResult ->
            Log.d(TAG, "getAuthorizationResultFromIntent succeeded: hasResolution=${authorizationResult.hasResolution()}, hasAccessToken=${authorizationResult.accessToken != null}")
            handleGoogleAuthorizationResult(authorizationResult)
        }.onFailure { throwable ->
            Log.e(TAG, "getAuthorizationResultFromIntent failed", throwable)
            if (throwable is ApiException) {
                viewModel.onGoogleDriveSignInFailed(throwable)
            } else {
                viewModel.onGoogleDriveSignInFailed(
                    if (throwable is Exception) throwable else Exception(throwable)
                )
            }
        }
    }

    private fun handleGoogleAuthorizationResult(
        authorizationResult: com.google.android.gms.auth.api.identity.AuthorizationResult
    ) {
        if (authorizationResult.hasResolution()) {
            val pendingIntent = authorizationResult.pendingIntent
            if (pendingIntent == null) {
                Log.e(TAG, "handleGoogleAuthorizationResult: hasResolution=true but pendingIntent is null")
                viewModel.onGoogleDriveSignInFailed(
                    IllegalStateException("Authorization requires resolution but no PendingIntent was provided")
                )
                return
            }
            Log.d(TAG, "handleGoogleAuthorizationResult: launching resolution pending intent")
            googleAuthorizationLauncher.launch(
                IntentSenderRequest.Builder(pendingIntent.intentSender).build()
            )
            return
        }

        Log.d(TAG, "handleGoogleAuthorizationResult: no resolution needed, fetching account email via userinfo")
        viewLifecycleOwner.lifecycleScope.launch {
            val accountEmail = runCatching {
                fetchUserEmailFromToken(authorizationResult.accessToken ?: return@runCatching null)
            }.getOrElse { throwable ->
                Log.e(TAG, "fetchUserEmailFromToken failed", throwable)
                viewModel.onGoogleDriveSignInFailed(
                    if (throwable is Exception) throwable else Exception(throwable)
                )
                null
            }

            if (accountEmail.isNullOrBlank()) {
                Log.e(TAG, "fetchUserEmailFromToken returned null/blank (accessToken was ${if (authorizationResult.accessToken != null) "present" else "null"})")
                viewModel.onGoogleDriveSignInFailed(
                    IllegalStateException("Google Drive authorization succeeded but account email was unavailable")
                )
            } else {
                Log.d(TAG, "Google Drive sign-in succeeded for account: $accountEmail")
                viewModel.onGoogleDriveSignedIn(accountEmail)
            }
        }
    }

    private suspend fun fetchUserEmailFromToken(accessToken: String): String? {
        return withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("https://www.googleapis.com/oauth2/v2/userinfo")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer $accessToken")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                val responseCode = connection.responseCode
                Log.d(TAG, "fetchUserEmailFromToken: response code=$responseCode")

                if (responseCode !in 200..299) {
                    Log.e(TAG, "fetchUserEmailFromToken: HTTP error $responseCode")
                    return@withContext null
                }

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val email = org.json.JSONObject(response).optString("email").takeIf { it.isNotBlank() }
                Log.d(TAG, "fetchUserEmailFromToken: email=${if (email != null) "present" else "null"}")
                email
            } catch (e: Exception) {
                Log.e(TAG, "fetchUserEmailFromToken: exception", e)
                null
            }
        }
    }

    companion object {
        private const val TAG = "BackupSettings"
    }
}
