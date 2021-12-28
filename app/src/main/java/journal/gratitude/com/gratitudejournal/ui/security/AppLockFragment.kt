package journal.gratitude.com.gratitudejournal.ui.security

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricConstants
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.presently.logging.AnalyticsLogger
import com.presently.logging.CrashReporter
import com.presently.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_CANCELLED
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_LOCKOUT
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_USER_CANCELLED
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import javax.inject.Inject

@AndroidEntryPoint
class AppLockFragment : Fragment() {

    private var fingerprintLock: Boolean = false

    @Inject lateinit var settings: PresentlySettings
    @Inject lateinit var analytics: AnalyticsLogger
    @Inject lateinit var crashReporter: CrashReporter

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fingerprintLock = settings.isBiometricsEnabled()
        if (!fingerprintLock) {
            val screen = activity?.intent?.extras?.getString(ContainerActivity.NOTIFICATION_SCREEN_EXTRA) ?: TIMELINE_SCREEN
            enterApp(screen)
        }

    }

    override fun onResume() {
        super.onResume()

        if (fingerprintLock)
            showFingerprintLock()
    }

    private fun showFingerprintLock() {
        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(
                            errorCode: Int,
                            errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)


                        when (errorCode) {
                            BiometricConstants.ERROR_NEGATIVE_BUTTON,
                            BiometricConstants.ERROR_USER_CANCELED -> {
                                analytics.recordEvent(BIOMETRICS_USER_CANCELLED)
                                requireActivity().finish()
                            }
                            // Occurs after a few failures,
                            // and blocks us from showing the biometric prompt for a few seconds
                            BiometricConstants.ERROR_LOCKOUT -> {
                                analytics.recordEvent(BIOMETRICS_LOCKOUT)
                                Toast.makeText(context, R.string.fingerprint_error_lockout_too_many, Toast.LENGTH_SHORT).show()
                                requireActivity().finish()
                            }
                            // After a few ERROR_LOCKOUTs,
                            // blocks the user from authenticating until other means of authentication is used successfully.
                            BiometricConstants.ERROR_LOCKOUT_PERMANENT -> {
                                //TODO move this hardcoded string to strings.xml
                                Toast.makeText(context, "Too many failed attempts.", Toast.LENGTH_SHORT).show()
                                crashReporter.logHandledException(Exception("Permanent Lockout occurred"))
                                requireActivity().finish()
                            }
                            BiometricConstants.ERROR_CANCELED -> {
                                //happens when the sensor is not available
                                //(happens onPause as well)
                                analytics.recordEvent(BIOMETRICS_CANCELLED)
                            }
                            BiometricConstants.ERROR_NO_BIOMETRICS,
                            BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL -> {
                                crashReporter.logHandledException(Exception(errString.toString()))
                                //no finger print is setup
                                //TODO move this hardcoded string to strings.xml
                                Toast.makeText(
                                        context,
                                        "Please set up a biometric recognition", Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().finish()
                            }
                            else -> {
                                crashReporter.logHandledException(Exception("Code: $errorCode: $errString"))
                                //TODO move this hardcoded string to strings.xml
                                Toast.makeText(
                                        context,
                                        "Authentication error code $errorCode", Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().finish()
                            }
                        }
                    }

                    override fun onAuthenticationSucceeded(
                            result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(result)
                        val screen = activity?.intent?.extras?.getString(ContainerActivity.NOTIFICATION_SCREEN_EXTRA) ?: TIMELINE_SCREEN
                        enterApp(screen)
                    }
                })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.lock_title))
                .setSubtitle(getString(R.string.lock_summary))
                .setNegativeButtonText(getString(R.string.cancel))
                .setConfirmationRequired(false)
                .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun enterApp(screenToOpen: String) {
        val fragment = when (screenToOpen) {
            TIMELINE_SCREEN -> TimelineFragment.newInstance()
            SETTINGS_SCREEN -> SettingsFragment()
            else -> throw IllegalArgumentException("Unknown screen to open")
        }

        parentFragmentManager
            .beginTransaction()
            .replace(R.id.container_fragment, fragment)
            .commit()
    }

    companion object {
        const val TIMELINE_SCREEN = "Timeline"
        const val SETTINGS_SCREEN = "Settings"
    }

}
