package journal.gratitude.com.gratitudejournal.ui.security

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class BiometricTelemetryTest {

    @Test
    fun `GIVEN biometric success WHEN availabilityDetails is called THEN details include mapped status`() {
        val details = BiometricTelemetry.availabilityDetails(
            statusCode = BiometricManager.BIOMETRIC_SUCCESS,
            source = BIOMETRIC_SOURCE_SETTINGS
        )

        assertThat(details["source"]).isEqualTo(BIOMETRIC_SOURCE_SETTINGS)
        assertThat(details["status_code"]).isEqualTo(BiometricManager.BIOMETRIC_SUCCESS)
        assertThat(details["status"]).isEqualTo("success")
        assertThat(details["authenticators"]).isEqualTo("weak_or_credential")
    }

    @Test
    fun `GIVEN biometric none enrolled WHEN availabilityDetails is called THEN details include mapped status`() {
        val details = BiometricTelemetry.availabilityDetails(
            statusCode = BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED,
            source = BIOMETRIC_SOURCE_APP_LOCK
        )

        assertThat(details["source"]).isEqualTo(BIOMETRIC_SOURCE_APP_LOCK)
        assertThat(details["status_code"]).isEqualTo(BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED)
        assertThat(details["status"]).isEqualTo("none_enrolled")
    }

    @Test
    fun `GIVEN biometric lockout error WHEN authErrorDetails is called THEN details include mapped error`() {
        val details = BiometricTelemetry.authErrorDetails(
            errorCode = BiometricPrompt.ERROR_LOCKOUT,
            source = BIOMETRIC_SOURCE_APP_LOCK
        )

        assertThat(details["source"]).isEqualTo(BIOMETRIC_SOURCE_APP_LOCK)
        assertThat(details["error_code"]).isEqualTo(BiometricPrompt.ERROR_LOCKOUT)
        assertThat(details["error"]).isEqualTo("lockout")
    }

    @Test
    fun `GIVEN biometric auth error WHEN authErrorMessage is called THEN message contains code and source`() {
        val message = BiometricTelemetry.authErrorMessage(
            errorCode = BiometricPrompt.ERROR_NO_BIOMETRICS,
            errString = "No biometrics enrolled",
            source = BIOMETRIC_SOURCE_APP_LOCK
        )

        assertThat(message).contains("app_lock")
        assertThat(message).contains("no_biometrics")
        assertThat(message).contains(BiometricPrompt.ERROR_NO_BIOMETRICS.toString())
    }
}
