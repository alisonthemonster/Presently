package journal.gratitude.com.gratitudejournal.reminders.troubleshooter.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "w411dp-h891dp-xxhdpi")
class NotificationTroubleshooterScreenUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun failedCheck_showsFailureCopy_andFixItButton() {
        setContent(
            NotificationTroubleshooterState(
                isLoading = false,
                checks = listOf(
                    NotificationTroubleshooterCheck.APP_NOTIFICATIONS.resultFor(false)
                )
            )
        )

        composeRule.onNodeWithTag("check_app_notifications").assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.status_failed)).assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.troubleshooter_failure_app_notifications))
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.fix_it)).assertIsDisplayed()
    }

    @Test
    fun allChecksPass_showsSupportFallback() {
        setContent(
            NotificationTroubleshooterState(
                isLoading = false,
                checks = NotificationTroubleshooterCheck.entries.map { it.resultFor(true) }
            )
        )

        composeRule.onNodeWithText(string(R.string.notification_troubleshooter_all_good))
            .assertIsDisplayed()
        composeRule.onNodeWithText(string(R.string.contact_support)).assertIsDisplayed()
    }

    private fun setContent(state: NotificationTroubleshooterState) {
        composeRule.setContent {
            PresentlyTheme {
                NotificationTroubleshooterScreenContent(
                    state = state,
                    onClose = {},
                    onFixItClicked = {},
                    onContactSupportClicked = {}
                )
            }
        }
    }

    private fun string(resId: Int): String =
        ApplicationProvider.getApplicationContext<android.content.Context>().getString(resId)
}
