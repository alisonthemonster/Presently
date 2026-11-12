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
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyTheme
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec

/**
 * Baselines are recorded on CI (Linux) but are also recorded and verified on developer Macs,
 * where native text rendering differs by a few hundred pixels per image (~0.01%). Allow up to
 * 0.1% of pixels to change so that doesn't fail verification. Real UI changes move far more
 * pixels than this (a moved or restyled element is typically several percent).
 */
internal val ScreenshotRoborazziOptions = RoborazziOptions(
    compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.001f)
)

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
        onRoot().captureRoboImage("$screen/$scenario/${themeSpec.name.lowercase()}.png", roborazziOptions = ScreenshotRoborazziOptions)
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
    onRoot().captureRoboImage(
        "$screen/${scenario}-${themeSpec.name.lowercase()}.png",
        roborazziOptions = ScreenshotRoborazziOptions
    )
}
