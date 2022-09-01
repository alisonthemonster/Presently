package com.presently.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography

@Immutable
data class PresentlyTypography(
    val titleLarge: TextStyle = TextStyle(
        fontSize = 28.sp,
        fontFamily = ValueFontFamily
    ),
    val titleMedium: TextStyle = TextStyle(
        fontSize = 22.sp,
        fontFamily = ValueFontFamily
    ),
    val titleSmall: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontFamily = ValueFontFamily
    ),
    val bodyLarge: TextStyle = TextStyle(
        fontSize = 20.sp,
        fontFamily = LarsseitFontFamily
    ),
    val bodyMedium: TextStyle = TextStyle(
        fontSize = 18.sp,
        fontFamily = LarsseitFontFamily
    ),
    val bodySmall: TextStyle = TextStyle(
        fontSize = 16.sp,
        fontFamily = LarsseitFontFamily
    ),
    val bodyExtraSmall: TextStyle = TextStyle(
        fontSize = 14.sp,
        fontFamily = LarsseitFontFamily
    )
)

@Stable
val LarsseitFontFamily = FontFamily(Font(R.font.larsseit_medium))

@Stable
val ValueFontFamily = FontFamily(Font(R.font.value_serif))

fun PresentlyTypography.toMaterialTheme(): Typography {
    return Typography(
        titleLarge = this.titleLarge,
        titleMedium = this.titleMedium,
        titleSmall = this.titleSmall,
        bodyLarge = this.bodyLarge,
        bodyMedium = this.bodySmall,
        bodySmall = this.bodySmall
    )
}