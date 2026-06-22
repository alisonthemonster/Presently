package journal.gratitude.com.gratitudejournal.ui.security

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import journal.gratitude.com.gratitudejournal.logging.AnalyticsLogger
import journal.gratitude.com.gratitudejournal.logging.CrashReporter
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_AUTH_SUCCEEDED
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_CANCELLED
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_PROMPT_ERROR
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_PROMPT_SHOWN
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_LOCKOUT
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_USER_CANCELLED
import androidx.glance.appwidget.updateAll
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment
import journal.gratitude.com.gratitudejournal.ui.timeline.TimelineFragment
import journal.gratitude.com.gratitudejournal.widget.RandomEntryWidget
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppLockFragment : Fragment() {

    private var fingerprintLock: Boolean = false
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    @Inject lateinit var settings: PresentlySettings
    @Inject lateinit var analytics: AnalyticsLogger
    @Inject lateinit var crashReporter: CrashReporter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    analytics.recordEvent(
                        BIOMETRICS_PROMPT_ERROR,
                        BiometricTelemetry.authErrorDetails(errorCode, BIOMETRIC_SOURCE_APP_LOCK)
                    )

                    when (errorCode) {
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON,
                        BiometricPrompt.ERROR_USER_CANCELED -> {
                            analytics.recordEvent(BIOMETRICS_USER_CANCELLED)
                            requireActivity().finish()
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            analytics.recordEvent(BIOMETRICS_LOCKOUT)
                            Toast.makeText(context, R.string.fingerprint_error_lockout_too_many, Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                        BiometricPrompt.ERROR_CANCELED -> {
                            analytics.recordEvent(BIOMETRICS_CANCELLED)
                        }
                        BiometricPrompt.ERROR_NO_BIOMETRICS,
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> {
                            crashReporter.logHandledException(
                                IllegalStateException(
                                    BiometricTelemetry.authErrorMessage(
                                        errorCode = errorCode,
                                        errString = errString,
                                        source = BIOMETRIC_SOURCE_APP_LOCK
                                    )
                                )
                            )
                            Toast.makeText(context, "Please set up a biometric recognition", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                        else -> {
                            crashReporter.logHandledException(
                                IllegalStateException(
                                    BiometricTelemetry.authErrorMessage(
                                        errorCode = errorCode,
                                        errString = errString,
                                        source = BIOMETRIC_SOURCE_APP_LOCK
                                    )
                                )
                            )
                            Toast.makeText(context, "Authentication error code $errorCode", Toast.LENGTH_SHORT).show()
                            requireActivity().finish()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    analytics.recordEvent(
                        BIOMETRICS_AUTH_SUCCEEDED,
                        mapOf("source" to BIOMETRIC_SOURCE_APP_LOCK)
                    )
                    settings.setOnPauseTime()

                    // Update widget after unlock to show entry content
                    context?.let { ctx ->
                        lifecycleScope.launch {
                            RandomEntryWidget().updateAll(ctx)
                        }
                    }

                    val screen = activity?.intent?.extras?.getString(ContainerActivity.NOTIFICATION_SCREEN_EXTRA) ?: TIMELINE_SCREEN
                    enterApp(screen)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(getString(R.string.lock_title))
            setSubtitle(getString(R.string.lock_summary))
            setConfirmationRequired(false)
            setAllowedAuthenticators(APP_LOCK_BIOMETRIC_AUTHENTICATORS)
        }.build()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fingerprintLock = settings.isBiometricsEnabled() && settings.shouldLockApp()
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
        val canAuthenticate = androidx.biometric.BiometricManager.from(requireContext())
            .canAuthenticate(APP_LOCK_BIOMETRIC_AUTHENTICATORS)
        analytics.recordEvent(
            BIOMETRICS_PROMPT_SHOWN,
            BiometricTelemetry.availabilityDetails(canAuthenticate, BIOMETRIC_SOURCE_APP_LOCK)
        )
        if (canAuthenticate != androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
            crashReporter.logHandledException(
                IllegalStateException(
                    BiometricTelemetry.availabilityMessage(
                        statusCode = canAuthenticate,
                        source = BIOMETRIC_SOURCE_APP_LOCK
                    )
                )
            )
        }
        biometricPrompt.authenticate(promptInfo)
    }

    private fun enterApp(screenToOpen: String) {
        val container = activity as? ContainerActivity

        val fragment = when (screenToOpen) {
            TIMELINE_SCREEN -> TimelineFragment.newInstance()
            SETTINGS_SCREEN -> SettingsFragment()
            ContainerActivity.WIDGET_ENTRY_SCREEN -> {
                val selectedDate = activity?.intent?.getStringExtra(RandomEntryWidget.EXTRA_SELECTED_DATE) ?: ""
                if (selectedDate.isNotEmpty() && container != null) {
                    // First, ensure the Timeline is the base fragment so "back" works correctly
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container_fragment, TimelineFragment.newInstance())
                        .commit()
                    // Then navigate to the specific entry (which adds to backstack)
                    container.navigateToEntry(selectedDate)
                } else {
                    // Fallback to Timeline if parsing fails
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container_fragment, TimelineFragment.newInstance())
                        .commit()
                }
                return
            }
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
