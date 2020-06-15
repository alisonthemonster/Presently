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
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_CANCELLED
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_LOCKOUT
import journal.gratitude.com.gratitudejournal.model.BIOMETRICS_USER_CANCELLED
import journal.gratitude.com.gratitudejournal.ui.settings.SettingsFragment.Companion.FINGERPRINT

class AppLockFragment : Fragment() {

    private var fingerprintLock: Boolean = false

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return View(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        fingerprintLock = sharedPref.getBoolean(FINGERPRINT, false)
        if (!fingerprintLock)
            moveToTimeline()

        firebaseAnalytics = FirebaseAnalytics.getInstance(context!!)
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

                        val crashlytics = FirebaseCrashlytics.getInstance()

                        when (errorCode) {
                            BiometricConstants.ERROR_NEGATIVE_BUTTON,
                            BiometricConstants.ERROR_USER_CANCELED -> {
                                firebaseAnalytics.logEvent(BIOMETRICS_USER_CANCELLED, null)
                                requireActivity().finish()
                            }
                            // Occurs after a few failures,
                            // and blocks us from showing the biometric prompt for a few seconds
                            BiometricConstants.ERROR_LOCKOUT -> {
                                firebaseAnalytics.logEvent(BIOMETRICS_LOCKOUT, null)
                                Toast.makeText(context, R.string.fingerprint_error_lockout_too_many, Toast.LENGTH_SHORT).show()
                                requireActivity().finish()
                            }
                            // After a few ERROR_LOCKOUTs,
                            // blocks the user from authenticating until other means of authentication is used successfully.
                            BiometricConstants.ERROR_LOCKOUT_PERMANENT -> {
                                Toast.makeText(context, "Too many failed attempts.", Toast.LENGTH_SHORT).show()
                                crashlytics.recordException(Exception("Permanent Lockout occurred"))
                                requireActivity().finish()
                            }
                            BiometricConstants.ERROR_CANCELED -> {
                                //happens when the sensor is not available
                                //(happens onPause as well)
                                firebaseAnalytics.logEvent(BIOMETRICS_CANCELLED, null)
                            }
                            BiometricConstants.ERROR_NO_BIOMETRICS,
                            BiometricConstants.ERROR_NO_DEVICE_CREDENTIAL -> {
                                crashlytics.recordException(Exception(errString.toString()))
                                //no finger print is setup
                                Toast.makeText(
                                        context,
                                        "Please set up a biometric recognition", Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().finish()
                            }
                            else -> {
                                crashlytics.recordException(Exception("Code: $errorCode: $errString"))
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
                        moveToTimeline()
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

    private fun moveToTimeline() {
        val navInflater = findNavController().navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)

        graph.startDestination = R.id.timelineFragment

        findNavController().graph = graph
    }

}
