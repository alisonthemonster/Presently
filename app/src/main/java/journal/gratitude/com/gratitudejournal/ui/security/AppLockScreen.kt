package journal.gratitude.com.gratitudejournal.ui.security

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsViewModel

@Composable
fun AppLockScreen(
    onUserAuthenticated: () -> Unit,
    onUserAuthenticationFailed: (Int?) -> Unit,
) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    var showDialog by remember { mutableStateOf(true) }

    Column() {
        Text("oh no")
        Text("you're locked out!")
        if (showDialog) {
            BiometricDialog(callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    showDialog = false

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            onUserAuthenticationFailed(null)
                        }
                        // Occurs after a few failures,
                        // and blocks us from showing the biometric prompt
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            onUserAuthenticationFailed(R.string.fingerprint_error_lockout_too_many)
                        }
                        BiometricPrompt.ERROR_CANCELED -> {
                            //happens when the sensor is not available
                            //(happens onPause as well)
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            onUserAuthenticationFailed(R.string.presently) //todo "Please set up a biometric recognition"
                        }
                        else -> {
                            //todo crashReporter.logHandledException(Exception("Code: $errorCode: $errString"))
                            onUserAuthenticationFailed(R.string.presently) //todo "Authentication error code $errorCode"
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    showDialog = false
                    super.onAuthenticationSucceeded(result)
                    viewModel.onAuthenticationSucceeded()
                    onUserAuthenticated()
                }
            })
        }
    }
}

@Composable
fun BiometricDialog(callback: BiometricPrompt.AuthenticationCallback) {
    val promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
        setTitle(stringResource(R.string.lock_title))
        setSubtitle(stringResource(R.string.lock_summary))
        setNegativeButtonText(stringResource(R.string.cancel))
        setConfirmationRequired(false)
    }.build()

    val activity = LocalContext.current as FragmentActivity
    val executor = ContextCompat.getMainExecutor(activity)
    BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
}