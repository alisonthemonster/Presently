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

@Composable
fun PresentlyTheme(
    themeSpec: PresentlyThemeSpec? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val tokens = remember(context, themeSpec) {
        context.resolvePresentlyThemeTokens(themeSpec)
    }
    val colorScheme = remember(tokens) {
        tokens.toColorScheme()
    }

    CompositionLocalProvider(LocalPresentlyTheme provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography(),
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
