package com.presently.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun PresentlyTheme(content: @Composable () -> Unit) {
    val typography = PresentlyTheme.typography
    CompositionLocalProvider(
        LocalPresentlyTypography provides typography
    ) {
        MaterialTheme(
            content = content,
            typography = typography.toMaterialTheme()
        )
    }
}


val LocalPresentlyTypography = staticCompositionLocalOf { PresentlyTypography() }

object PresentlyTheme {
    val typography: PresentlyTypography
        @Composable
        get() = LocalPresentlyTypography.current
}