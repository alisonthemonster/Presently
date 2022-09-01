package com.presently.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color

@Composable
fun PresentlyTheme(
    selectedTheme: PresentlyColors,
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

//following the JetSnack Compose sample to discourage the use of MaterialTheme
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
        scrim = this.debugColor1,
    )
}


class PresentlyColors(
    backgroundColor: Color,
    textColor: Color,
    inverseBackgroundColor: Color,
    inverseTextColor: Color,
    debugColor1: Color,
    debugColor2: Color,
    debugColor3: Color
) {
    var backgroundColor by mutableStateOf(backgroundColor)
        private set

    var textColor by mutableStateOf(textColor)
        private set

    var inverseBackgroundColor by mutableStateOf(inverseBackgroundColor)
        private set

    var inverseTextColor by mutableStateOf(inverseTextColor)
        private set

    var debugColor1 by mutableStateOf(debugColor1)
        private set

    var debugColor2 by mutableStateOf(debugColor2)
        private set

    var debugColor3 by mutableStateOf(debugColor3)
        private set

    fun update(other: PresentlyColors) {
        backgroundColor = other.backgroundColor
        textColor = other.textColor
        inverseBackgroundColor = other.inverseBackgroundColor
        inverseTextColor = other.inverseTextColor
    }
}

val OriginalColors = PresentlyColors(
    backgroundColor = Color(0xffdbd1c7),
    textColor = Color(0xff000000),
    inverseBackgroundColor = Color(0xff000000),
    inverseTextColor = Color(0xffdbd1c7),
    debugColor1 = Color(0xffDFA700),
    debugColor2 = Color(0xff510998),
    debugColor3 = Color(0xffFD6574),
)


val CalmColors = PresentlyColors(
    backgroundColor = Color(0xffBBC4FB),
    textColor = Color(0x697AD6),
    inverseBackgroundColor = Color(0xffFFFFFF),
    inverseTextColor = Color(0xffBAC6FF),
    debugColor1 = Color(0xffDFA700),
    debugColor2 = Color(0xff510998),
    debugColor3 = Color(0xff65E6FD),
)


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

fun String.toPresentlyColors(): PresentlyColors {
    return when (this) {
        "Calm" -> CalmColors
        else -> OriginalColors
    }
}