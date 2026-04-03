package journal.gratitude.com.gratitudejournal.testUtils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec

data class ScreenshotDevice(
    val widthDp: Int,
    val heightDp: Int
)

object ScreenshotDevices {
    val MediumPhone = ScreenshotDevice(
        widthDp = 411,
        heightDp = 891
    )

    val TabletPortrait = ScreenshotDevice(
        widthDp = 800,
        heightDp = 1280
    )
}

fun ComposeContentTestRule.captureAcrossThemes(
    screen: String,
    scenario: String,
    device: ScreenshotDevice = ScreenshotDevices.MediumPhone,
    content: @Composable (PresentlyThemeSpec) -> Unit
) {
    var currentTheme by mutableStateOf(PresentlyThemeSpec.entries.first())

    setContent {
        Box(
            modifier = Modifier.requiredSize(
                width = device.widthDp.dp,
                height = device.heightDp.dp
            )
        ) {
            PresentlyTheme(themeSpec = currentTheme) {
                content(currentTheme)
            }
        }
    }

    PresentlyThemeSpec.entries.forEach { themeSpec ->
        runOnIdle {
            currentTheme = themeSpec
        }
        waitForIdle()
        onRoot().captureRoboImage("$screen/$scenario/${themeSpec.name.lowercase()}.png")
    }
}

fun ComposeContentTestRule.captureInTheme(
    screen: String,
    scenario: String,
    themeSpec: PresentlyThemeSpec,
    device: ScreenshotDevice = ScreenshotDevices.MediumPhone,
    content: @Composable () -> Unit
) {
    setContent {
        Box(
            modifier = Modifier.requiredSize(
                width = device.widthDp.dp,
                height = device.heightDp.dp
            )
        ) {
            PresentlyTheme(themeSpec = themeSpec) {
                content()
            }
        }
    }

    waitForIdle()
    onRoot().captureRoboImage("$screen/${scenario}-${themeSpec.name.lowercase()}.png")
}
