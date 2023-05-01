package com.presently.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun PresentlyTheme(
    selectedTheme: PresentlyColors = OriginalColors,
    content: @Composable () -> Unit
) {
    val typography = PresentlyTheme.typography
    val colors = remember { selectedTheme }
    colors.update(selectedTheme)

    CompositionLocalProvider(
        LocalPresentlyTypography provides typography,
        LocalPresentlyColors provides colors
    ) {
        MaterialTheme(
            content = content,
            typography = typography.toMaterialTheme(),
            colorScheme = colors.toMaterialTheme()
        )
    }
}

// following the JetSnack Compose sample to discourage the use of MaterialTheme
private fun PresentlyColors.toMaterialTheme(): ColorScheme {
    return ColorScheme(
        primary = this.debugColor1,
        onPrimary = this.debugColor1,
        primaryContainer = this.debugColor1,
        onPrimaryContainer = this.debugColor1,
        inversePrimary = this.debugColor1,
        secondary = this.debugColor1,
        onSecondary = this.debugColor1,
        secondaryContainer = this.debugColor1,
        onSecondaryContainer = this.debugColor1,
        tertiary = this.debugColor1,
        onTertiary = this.debugColor1,
        tertiaryContainer = this.debugColor1,
        onTertiaryContainer = this.debugColor1,
        background = this.debugColor1,
        onBackground = this.debugColor1,
        surface = this.debugColor1,
        onSurface = this.debugColor1,
        surfaceVariant = this.debugColor1,
        onSurfaceVariant = this.debugColor1,
        surfaceTint = this.debugColor1,
        inverseSurface = this.debugColor1,
        inverseOnSurface = this.debugColor1,
        error = this.debugColor1,
        onError = this.debugColor1,
        errorContainer = this.debugColor1,
        onErrorContainer = this.debugColor1,
        outline = this.debugColor1,
        outlineVariant = this.debugColor1,
        scrim = this.debugColor1
    )
}

private val LocalPresentlyTypography = staticCompositionLocalOf { PresentlyTypography() }
private val LocalPresentlyColors = staticCompositionLocalOf<PresentlyColors> {
    error("No colors provided!")
}

object PresentlyTheme {
    val typography: PresentlyTypography
        @Composable
        get() = LocalPresentlyTypography.current

    val colors: PresentlyColors
        @Composable
        get() = LocalPresentlyColors.current
}
