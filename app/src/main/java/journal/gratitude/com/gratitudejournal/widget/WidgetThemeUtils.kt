package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ContextThemeWrapper
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.createBitmap
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec

internal data class ThemeAssets(
    val backgroundColor: Color,
    val textColor: Color,
    val hintColor: Color,
    val iconBitmap: Bitmap,
)

internal fun loadThemeAssets(context: Context, themeName: String): ThemeAssets {
    val theme = PresentlyThemeSpec.fromStorageValue(themeName)
    val themedContext = ContextThemeWrapper(context, theme.styleRes)
    val attrs = themedContext.obtainStyledAttributes(
        intArrayOf(
            R.attr.timelineBackgroundColor,
            R.attr.timelineHeaderColor,
            R.attr.timelineHintColor,
            R.attr.timelineIcon,
        )
    )
    val backgroundColor = Color(attrs.getColor(0, 0xFFDBD1C7.toInt()))
    val textColor = Color(attrs.getColor(1, 0xFF000000.toInt()))
    val hintColor = Color(attrs.getColor(2, 0xFF79736A.toInt()))
    val iconResId = attrs.getResourceId(3, R.drawable.ic_flower)
    attrs.recycle()

    val iconBitmap = rasterizeDrawable(themedContext, iconResId, 192)
    return ThemeAssets(backgroundColor, textColor, hintColor, iconBitmap)
}

private fun rasterizeDrawable(context: Context, resId: Int, sizePx: Int): Bitmap {
    val drawable = AppCompatResources.getDrawable(context, resId)
        ?: return createBitmap(sizePx, sizePx)
    val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: sizePx
    val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: sizePx
    val scale = sizePx.toFloat() / maxOf(intrinsicW, intrinsicH)
    val w = (intrinsicW * scale).toInt().coerceAtLeast(1)
    val h = (intrinsicH * scale).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(w, h)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, w, h)
    drawable.draw(canvas)
    return bitmap
}
