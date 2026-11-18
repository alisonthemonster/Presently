package journal.gratitude.com.gratitudejournal.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.glance.Image
import androidx.glance.ImageProvider
import kotlin.math.ceil
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.ContentScale
import androidx.glance.layout.size
import androidx.appcompat.content.res.AppCompatResources
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.semantics.contentDescription
import androidx.glance.semantics.semantics
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.SettingsEntryPoint
import journal.gratitude.com.gratitudejournal.di.WidgetEntryPoint
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import kotlinx.coroutines.flow.first
import java.text.NumberFormat

/** A small home screen widget showing how many journal entries the user has written. */
class EntryCountWidget : GlanceAppWidget() {

    companion object {
        private val SMALL = DpSize(110.dp, 110.dp)
        private val WIDE = DpSize(250.dp, 110.dp)
        private val BIG = DpSize(250.dp, 250.dp)

        private const val PREVIEW_ENTRY_COUNT = 214
        private const val PREVIEW_THEME = "original"
    }

    override val sizeMode = SizeMode.Exact
    override val previewSizeMode = SizeMode.Responsive(setOf(SMALL, WIDE, BIG))

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val colors = loadColors(context, PREVIEW_THEME)
        provideContent { EntryCountContent(PREVIEW_ENTRY_COUNT, colors) }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = EntryPointAccessors
            .fromApplication(context, SettingsEntryPoint::class.java)
            .settings
        val colors = loadColors(context, settings.getCurrentTheme())
        val entryCount = EntryPointAccessors
            .fromApplication(context, WidgetEntryPoint::class.java)
            .entryDao
            .getEntryCount()
            .first()

        provideContent { EntryCountContent(entryCount, colors) }
    }

    @SuppressLint("ResourceType")
    private fun loadColors(context: Context, themeName: String): EntryCountColors {
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
        val colors = EntryCountColors(
            background = Color(attrs.getColor(0, 0xFFDBD1C7.toInt())),
            text = Color(attrs.getColor(1, 0xFF000000.toInt())),
            hint = Color(attrs.getColor(2, 0xFF79736A.toInt())),
            icon = rasterizeDrawable(themedContext, attrs.getResourceId(3, R.drawable.ic_flower)),
        )
        attrs.recycle()
        return colors
    }
}

private val PADDING = 8.dp

private data class EntryCountColors(
    val background: Color,
    val text: Color,
    val hint: Color,
    val icon: Bitmap,
)

private const val ICON_SIZE_PX = 192

private fun rasterizeDrawable(context: Context, resId: Int): Bitmap {
    val drawable = AppCompatResources.getDrawable(context, resId)
        ?: return createBitmap(ICON_SIZE_PX, ICON_SIZE_PX)
    val intrinsicW = drawable.intrinsicWidth.takeIf { it > 0 } ?: ICON_SIZE_PX
    val intrinsicH = drawable.intrinsicHeight.takeIf { it > 0 } ?: ICON_SIZE_PX
    val scale = ICON_SIZE_PX.toFloat() / maxOf(intrinsicW, intrinsicH)
    val w = (intrinsicW * scale).toInt().coerceAtLeast(1)
    val h = (intrinsicH * scale).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(w, h)
    drawable.setBounds(0, 0, w, h)
    drawable.draw(Canvas(bitmap))
    return bitmap
}

@Composable
private fun EntryCountContent(count: Int, colors: EntryCountColors) {
    val context = LocalContext.current
    val resources = context.resources
    val description = resources.getQuantityString(R.plurals.widget_entries_description, count, count)
    val size = LocalSize.current
    val density = context.resources.displayMetrics.density
    val availableWidthPx = ((size.width - PADDING * 2).value * density).toInt()
    // Scale the text with the widget so it fills the space, shrinking only if it would overflow.
    val shortSideDp = minOf(size.width.value, size.height.value)
    val numberSizeSp = shortSideDp * 0.38f
    val iconSizeDp = shortSideDp * 0.22f
    val labelSizeSp = shortSideDp * 0.13f
    val numberText = NumberFormat.getInstance().format(count)
    val labelText = resources.getQuantityString(R.plurals.widget_entries_label, count)

    // Glance text can't use app font resources, so the text is drawn to bitmaps with the
    // app's own fonts. The content description keeps it readable for screen readers.
    val numberBitmap = renderText(context, numberText, R.font.value, numberSizeSp, colors.text, availableWidthPx)
    val labelBitmap = renderText(context, labelText, R.font.larsseit_medium, labelSizeSp, colors.hint, availableWidthPx)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colors.background)
            .cornerRadius(16.dp)
            .clickable(
                actionStartActivity<ContainerActivity>(
                    parameters = actionParametersOf(
                        GratitudeQuoteWidget.cameFromWidgetKey to true
                    )
                )
            )
            .padding(PADDING)
            .semantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(colors.icon),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier.size(iconSizeDp.dp),
        )
        Image(provider = ImageProvider(numberBitmap), contentDescription = null)
        Image(provider = ImageProvider(labelBitmap), contentDescription = null)
    }
}

private fun renderText(
    context: Context,
    text: String,
    @FontRes fontRes: Int,
    sizeSp: Float,
    color: Color,
    maxWidthPx: Int,
): Bitmap {
    val density = context.resources.displayMetrics.density
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = ResourcesCompat.getFont(context, fontRes) ?: Typeface.DEFAULT
        textSize = sizeSp * density
        this.color = color.toArgb()
    }
    val naturalWidth = paint.measureText(text)
    if (naturalWidth > maxWidthPx && maxWidthPx > 0) {
        paint.textSize *= maxWidthPx / naturalWidth
    }
    val fontMetrics = paint.fontMetrics
    val width = ceil(paint.measureText(text)).toInt().coerceAtLeast(1)
    val height = ceil(fontMetrics.descent - fontMetrics.ascent).toInt().coerceAtLeast(1)
    val bitmap = createBitmap(width, height)
    Canvas(bitmap).drawText(text, 0f, -fontMetrics.ascent, paint)
    return bitmap
}
