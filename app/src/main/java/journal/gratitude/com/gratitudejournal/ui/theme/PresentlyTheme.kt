package journal.gratitude.com.gratitudejournal.ui.theme

import android.content.Context
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.annotation.AttrRes
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import journal.gratitude.com.gratitudejournal.R

@Immutable
data class PresentlyThemeTokens(
    val timelineIconRes: Int,
    val timelineBackground: Color,
    val timelineLine: Color,
    val timelineHeader: Color,
    val timelineBody: Color,
    val timelineHint: Color,
    val toolbar: Color,
    val toolbarItem: Color,
    val statusBar: Color,
    val entryBackground: Color,
    val entryHeader: Color,
    val entryBody: Color,
    val entryHint: Color,
    val highlight: Color,
    val fab: Color,
    val fabText: Color
) {
    fun toColorScheme(): ColorScheme = lightColorScheme(
        primary = toolbar,
        onPrimary = toolbarItem,
        secondary = highlight,
        onSecondary = timelineBackground,
        background = timelineBackground,
        onBackground = timelineBody,
        surface = timelineBackground,
        onSurface = timelineBody,
        surfaceVariant = entryBackground,
        onSurfaceVariant = entryBody
    )
}

val LocalPresentlyTheme = staticCompositionLocalOf<PresentlyThemeTokens> {
    error("PresentlyThemeTokens not provided")
}

object PresentlyFontFamilies {
    val body = FontFamily(Font(R.font.larsseit_medium))
    val accent = FontFamily(Font(R.font.value_serif))
}

private val PresentlyTypography = Typography().run {
    copy(
        bodyLarge = bodyLarge.copy(fontFamily = PresentlyFontFamilies.body),
        bodyMedium = bodyMedium.copy(fontFamily = PresentlyFontFamilies.body),
        bodySmall = bodySmall.copy(fontFamily = PresentlyFontFamilies.body),
        labelLarge = labelLarge.copy(fontFamily = PresentlyFontFamilies.body),
        labelMedium = labelMedium.copy(fontFamily = PresentlyFontFamilies.body),
        labelSmall = labelSmall.copy(fontFamily = PresentlyFontFamilies.body),
        titleLarge = titleLarge.copy(fontFamily = PresentlyFontFamilies.accent),
        titleMedium = titleMedium.copy(fontFamily = PresentlyFontFamilies.accent),
        titleSmall = titleSmall.copy(fontFamily = PresentlyFontFamilies.accent),
        headlineLarge = headlineLarge.copy(fontFamily = PresentlyFontFamilies.accent),
        headlineMedium = headlineMedium.copy(fontFamily = PresentlyFontFamilies.accent),
        headlineSmall = headlineSmall.copy(fontFamily = PresentlyFontFamilies.accent)
    )
}

@Composable
fun PresentlyTheme(
    themeSpec: PresentlyThemeSpec? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themedContext = remember(context, themeSpec) {
        if (themeSpec == null) {
            context
        } else {
            ContextThemeWrapper(context, themeSpec.styleRes)
        }
    }
    val tokens = remember(themedContext) {
        themedContext.resolvePresentlyThemeTokens()
    }
    val colorScheme = remember(tokens) {
        tokens.toColorScheme()
    }

    CompositionLocalProvider(
        LocalContext provides themedContext,
        LocalPresentlyTheme provides tokens
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = PresentlyTypography,
            content = content
        )
    }
}

fun Context.resolvePresentlyThemeTokens(
    themeSpec: PresentlyThemeSpec? = null
): PresentlyThemeTokens {
    val themedContext = if (themeSpec == null) {
        this
    } else {
        ContextThemeWrapper(this, themeSpec.styleRes)
    }

    return PresentlyThemeTokens(
        timelineIconRes = themedContext.resolveResourceAttr(R.attr.timelineIcon),
        timelineBackground = Color(themedContext.resolveColorAttr(R.attr.timelineBackgroundColor)),
        timelineLine = Color(themedContext.resolveColorAttr(R.attr.timelineLineColor)),
        timelineHeader = Color(themedContext.resolveColorAttr(R.attr.timelineHeaderColor)),
        timelineBody = Color(themedContext.resolveColorAttr(R.attr.timelineBodyColor)),
        timelineHint = Color(themedContext.resolveColorAttr(R.attr.timelineHintColor)),
        toolbar = Color(themedContext.resolveColorAttr(R.attr.toolbarColor)),
        toolbarItem = Color(themedContext.resolveColorAttr(R.attr.toolbarItemColor)),
        statusBar = Color(themedContext.resolveColorAttr(R.attr.statusBarColor)),
        entryBackground = Color(themedContext.resolveColorAttr(R.attr.entryBackgroundColor)),
        entryHeader = Color(themedContext.resolveColorAttr(R.attr.entryHeaderColor)),
        entryBody = Color(themedContext.resolveColorAttr(R.attr.entryBodyColor)),
        entryHint = Color(themedContext.resolveColorAttr(R.attr.entryHintColor)),
        highlight = Color(themedContext.resolveColorAttr(R.attr.highlightColor)),
        fab = Color(themedContext.resolveColorAttr(R.attr.fabColor)),
        fabText = Color(themedContext.resolveColorAttr(R.attr.fabTextColor))
    )
}

private fun Context.resolveColorAttr(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.data
}

private fun Context.resolveResourceAttr(@AttrRes attrRes: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attrRes, typedValue, true)
    return typedValue.resourceId
}
