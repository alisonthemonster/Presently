package journal.gratitude.com.gratitudejournal.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt

internal const val APP_LOCK_BIOMETRIC_AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
internal const val BIOMETRIC_SOURCE_APP_LOCK = "app_lock"
internal const val BIOMETRIC_SOURCE_SETTINGS = "settings"

internal object BiometricTelemetry {

    fun availabilityDetails(statusCode: Int, source: String): Map<String, Any> {
        return mapOf(
            "source" to source,
            "status_code" to statusCode,
            "status" to availabilityStatus(statusCode),
            "authenticators" to "weak_or_credential"
        )
    }

    fun authErrorDetails(errorCode: Int, source: String): Map<String, Any> {
        return mapOf(
            "source" to source,
            "error_code" to errorCode,
            "error" to authError(errorCode)
        )
    }

    fun availabilityMessage(statusCode: Int, source: String): String {
        return "Biometric availability check failed from $source with ${availabilityStatus(statusCode)} ($statusCode)"
    }

    fun authErrorMessage(errorCode: Int, errString: CharSequence, source: String): String {
        return "Biometric auth error from $source with ${authError(errorCode)} ($errorCode): $errString"
    }

    internal fun availabilityStatus(statusCode: Int): String {
        return when (statusCode) {
            BiometricManager.BIOMETRIC_SUCCESS -> "success"
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> "status_unknown"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "unsupported"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "hardware_unavailable"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "none_enrolled"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "no_hardware"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "security_update_required"
            else -> "unknown"
        }
    }

    internal fun authError(errorCode: Int): String {
        return when (errorCode) {
            BiometricPrompt.ERROR_CANCELED -> "canceled"
            BiometricPrompt.ERROR_HW_NOT_PRESENT -> "hardware_not_present"
            BiometricPrompt.ERROR_HW_UNAVAILABLE -> "hardware_unavailable"
            BiometricPrompt.ERROR_LOCKOUT -> "lockout"
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> "lockout_permanent"
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> "negative_button"
            BiometricPrompt.ERROR_NO_BIOMETRICS -> "no_biometrics"
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> "no_device_credential"
            BiometricPrompt.ERROR_NO_SPACE -> "no_space"
            BiometricPrompt.ERROR_TIMEOUT -> "timeout"
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS -> "unable_to_process"
            BiometricPrompt.ERROR_USER_CANCELED -> "user_canceled"
            BiometricPrompt.ERROR_VENDOR -> "vendor"
            else -> "unknown"
        }
    }
}
