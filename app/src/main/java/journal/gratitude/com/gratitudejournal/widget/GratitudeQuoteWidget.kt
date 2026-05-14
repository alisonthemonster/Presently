package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.ContextThemeWrapper
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.layout.ContentScale
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Alignment.Vertical
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.preference.PreferenceManager
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.model.THEME_PREF
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import org.threeten.bp.LocalDate
import androidx.core.graphics.createBitmap

class GratitudeQuoteWidget : GlanceAppWidget() {

    companion object {
        val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        val TALL = DpSize(100.dp, 250.dp)
        val BIG_SQUARE = DpSize(250.dp, 250.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE)
    )

    override val previewSizeMode = SizeMode.Responsive(
        setOf(HORIZONTAL_RECTANGLE, BIG_SQUARE)
    )

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val theme = PresentlyThemeSpec.fromStorageValue("original")
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

        provideContent {
            WidgetContent(
                quote = "\"Gratitude turns what we have into enough.\"",
                author = "Melody Beattie",
                backgroundColor = backgroundColor,
                textColor = textColor,
                hintColor = hintColor,
                iconBitmap = iconBitmap,
            )
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val themeName = prefs.getString(THEME_PREF, "original") ?: "original"
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

        val quotes = context.resources.getStringArray(R.array.inspirations)
        val index = LocalDate.now().dayOfYear % quotes.size
        val (quoteText, author) = parseQuote(quotes[index])

        provideContent {
            WidgetContent(
                quote = quoteText,
                author = author,
                backgroundColor = backgroundColor,
                textColor = textColor,
                hintColor = hintColor,
                iconBitmap = iconBitmap,
            )
        }
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

    private fun parseQuote(raw: String): Pair<String, String> {
        val idx = raw.lastIndexOf('\n')
        return if (idx >= 0) {
            raw.substring(0, idx).trim() to raw.substring(idx + 1).trim()
        } else {
            raw.trim() to ""
        }
    }


}

@Composable
private fun WidgetContent(
    quote: String,
    author: String,
    backgroundColor: Color,
    textColor: Color,
    hintColor: Color,
    iconBitmap: Bitmap,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<ContainerActivity>())
            .padding(12.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        val size = LocalSize.current
        when {
            size.width >= GratitudeQuoteWidget.BIG_SQUARE.width && size.height >= GratitudeQuoteWidget.BIG_SQUARE.height ->
                TallQuoteLayout(quote, author, iconBitmap, textColor, hintColor, quoteFontSize = 20, authorFontSize = 14)
            size.height >= GratitudeQuoteWidget.TALL.height ->
                TallQuoteLayout(quote, author, iconBitmap, textColor, hintColor, quoteFontSize = 16, authorFontSize = 12)
            size.width >= GratitudeQuoteWidget.HORIZONTAL_RECTANGLE.width ->
                WideQuoteLayout(quote, author, iconBitmap, textColor, hintColor, quoteFontSize = 18, authorFontSize = 13)
            else ->
                QuoteLayout(quote, author, iconBitmap, textColor, hintColor, quoteFontSize = 14, authorFontSize = 12)
        }
    }
}

@Composable
private fun QuoteLayout(
    quote: String,
    author: String,
    iconBitmap: Bitmap,
    textColor: Color,
    hintColor: Color,
    quoteFontSize: Int,
    authorFontSize: Int,
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = quote,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = quoteFontSize.sp,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(iconBitmap),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxSize(),
            )
        }
        if (author.isNotEmpty()) {
            Text(
                text = author,
                style = TextStyle(
                    color = ColorProvider(hintColor),
                    fontSize = authorFontSize.sp,
                    textAlign = TextAlign.End,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WideQuoteLayout(
    quote: String,
    author: String,
    iconBitmap: Bitmap,
    textColor: Color,
    hintColor: Color,
    quoteFontSize: Int,
    authorFontSize: Int,
) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = quote,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = quoteFontSize.sp,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(iconBitmap),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxSize(),
            )
        }
        if (author.isNotEmpty()) {
            Text(
                text = author,
                style = TextStyle(
                    color = ColorProvider(hintColor),
                    fontSize = authorFontSize.sp,
                    textAlign = TextAlign.End,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TallQuoteLayout(
    quote: String,
    author: String,
    iconBitmap: Bitmap,
    textColor: Color,
    hintColor: Color,
    quoteFontSize: Int,
    authorFontSize: Int,
) {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = quote,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = quoteFontSize.sp,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        Box(
            modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(iconBitmap),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = GlanceModifier.fillMaxSize(),
            )
        }
        if (author.isNotEmpty()) {
            Text(
                text = author,
                style = TextStyle(
                    color = ColorProvider(hintColor),
                    fontSize = authorFontSize.sp,
                    textAlign = TextAlign.End,
                ),
                modifier = GlanceModifier.fillMaxWidth(),
            )
        }
    }
}
