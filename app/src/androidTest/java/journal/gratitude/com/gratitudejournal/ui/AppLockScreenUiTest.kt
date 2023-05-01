package journal.gratitude.com.gratitudejournal.ui

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.common.truth.Truth.assertThat
import com.presently.ui.PresentlyTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import journal.gratitude.com.gratitudejournal.MainActivity
import journal.gratitude.com.gratitudejournal.ui.security.AppLockScreen
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class AppLockScreenUiTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun testLockScreen() {
        var errorResource: String? = ""

        composeTestRule.setContent {
            PresentlyTheme {
                AppLockScreen(
                    onUserAuthenticated = {},
                    onUserAuthenticationFailed = { errString ->
                        errorResource = errString
                    }
                )
            }
        }

        assertThat(errorResource).isEqualTo("No fingerprints enrolled.")
    }
}
