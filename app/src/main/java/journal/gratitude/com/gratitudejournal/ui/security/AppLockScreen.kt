package journal.gratitude.com.gratitudejournal.ui.security

import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.presently.ui.PresentlyTheme
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsViewModel

@Composable
fun AppLockScreen(
    modifier: Modifier = Modifier,
    onUserAuthenticated: () -> Unit,
    onUserAuthenticationFailed: (String?) -> Unit,
) {
    val viewModel = hiltViewModel<SettingsViewModel>()
    var showDialog by remember { mutableStateOf(true) }

    val theme = remember { viewModel.getSelectedTheme() }

    PresentlyTheme(
        selectedTheme = theme,
    ) {
        Surface(
            color = PresentlyTheme.colors.timelineBackground,
            modifier = modifier
                .windowInsetsPadding(WindowInsets.statusBars)
                .fillMaxHeight(),
        ) {
            Column(
                modifier = modifier.fillMaxSize().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.lock_summary),
                    style = PresentlyTheme.typography.titleLarge,
                    color = PresentlyTheme.colors.timelineLogo,
                    textAlign = TextAlign.Center,
                )
                Image(
                    modifier = modifier.size(80.dp).padding(8.dp),
                    painter = painterResource(id = theme.iconResource),
                    contentDescription = null,
                )
                if (showDialog) {
                    BiometricDialog(
                        callback = object : BiometricPrompt.AuthenticationCallback() {
                            override fun onAuthenticationError(
                                errorCode: Int,
                                errString: CharSequence,
                            ) {
                                super.onAuthenticationError(errorCode, errString)
                                showDialog = false

                                when (errorCode) {
                                    BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                                    BiometricPrompt.ERROR_USER_CANCELED,
                                    -> {
                                        onUserAuthenticationFailed(null)
                                    }
                                    // Occurs after a few failures,
                                    // and blocks us from showing the biometric prompt
                                    BiometricPrompt.ERROR_LOCKOUT,
                                    BiometricPrompt.ERROR_LOCKOUT_PERMANENT,
                                    -> {
                                        onUserAuthenticationFailed(errString.toString())
                                    }
                                    BiometricPrompt.ERROR_CANCELED -> {
                                        // happens when the sensor is not available
                                        // (happens onPause as well)
                                    }
                                    BiometricPrompt.ERROR_NO_BIOMETRICS,
                                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL,
                                    -> {
                                        onUserAuthenticationFailed(errString.toString())
                                    }
                                    else -> {
                                        viewModel.onUnknownAuthenticationError(errorCode, errString)
                                        onUserAuthenticationFailed(errString.toString())
                                    }
                                }
                            }

                            override fun onAuthenticationSucceeded(
                                result: BiometricPrompt.AuthenticationResult,
                            ) {
                                showDialog = false
                                super.onAuthenticationSucceeded(result)
                                viewModel.onAuthenticationSucceeded()
                                onUserAuthenticated()
                            }
                        },
                    )
                }
            }
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
