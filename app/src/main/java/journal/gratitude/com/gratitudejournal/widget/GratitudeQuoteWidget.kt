package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.ContentScale
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.SettingsEntryPoint
import journal.gratitude.com.gratitudejournal.model.CAME_FROM_WIDGET
import org.threeten.bp.LocalDate

class GratitudeQuoteWidget : GlanceAppWidget() {

    companion object {
        val cameFromWidgetKey = ActionParameters.Key<Boolean>(CAME_FROM_WIDGET)

        val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        val TALL = DpSize(100.dp, 250.dp)
        val BIG_SQUARE = DpSize(250.dp, 250.dp)

        private const val PREVIEW_QUOTE = "\"Gratitude turns what we have into enough.\""
        private const val PREVIEW_AUTHOR = "Melody Beattie"
        private const val PREVIEW_THEME = "original"
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE)
    )

    override val previewSizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE)
    )

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val assets = loadThemeAssets(context, PREVIEW_THEME)
        provideContent {
            WidgetContent(
                quote = PREVIEW_QUOTE,
                author = PREVIEW_AUTHOR,
                assets = assets,
            )
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = EntryPointAccessors
            .fromApplication(context, SettingsEntryPoint::class.java)
            .settings
        val assets = loadThemeAssets(context, settings.getCurrentTheme())

        val quotes = context.resources.getStringArray(R.array.inspirations)
        val index = selectQuoteIndex(LocalDate.now().dayOfYear, quotes.size)
        val (quoteText, author) = parseQuote(quotes[index])

        provideContent {
            WidgetContent(
                quote = quoteText,
                author = author,
                assets = assets,
            )
        }
    }
}

@Composable
private fun WidgetContent(
    quote: String,
    author: String,
    assets: ThemeAssets,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(assets.backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<ContainerActivity>(
                parameters = actionParametersOf(GratitudeQuoteWidget.cameFromWidgetKey to true)
            ))
            .padding(12.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        val size = LocalSize.current
        val isWide = size.width >= GratitudeQuoteWidget.HORIZONTAL_RECTANGLE.width &&
            size.height < GratitudeQuoteWidget.TALL.height
        val (quoteFontSize, authorFontSize) = when {
            size.width >= GratitudeQuoteWidget.BIG_SQUARE.width &&
                size.height >= GratitudeQuoteWidget.BIG_SQUARE.height -> 20 to 14
            size.height >= GratitudeQuoteWidget.TALL.height -> 16 to 12
            isWide -> 18 to 13
            else -> 14 to 12
        }
        QuoteLayout(
            quote = quote,
            author = author,
            assets = assets,
            quoteFontSize = quoteFontSize,
            authorFontSize = authorFontSize,
            centerHorizontally = !isWide,
            iconPadding = if (isWide) 16.dp else 8.dp,
        )
    }
}

@Composable
private fun QuoteLayout(
    quote: String,
    author: String,
    assets: ThemeAssets,
    quoteFontSize: Int,
    authorFontSize: Int,
    centerHorizontally: Boolean,
    iconPadding: Dp,
) {
    val columnModifier = GlanceModifier.fillMaxSize()
    if (centerHorizontally) {
        Column(
            modifier = columnModifier,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            QuoteLayoutContent(quote, author, assets, quoteFontSize, authorFontSize, iconPadding)
        }
    } else {
        Column(modifier = columnModifier) {
            QuoteLayoutContent(quote, author, assets, quoteFontSize, authorFontSize, iconPadding)
        }
    }
}

@Composable
private fun ColumnScope.QuoteLayoutContent(
    quote: String,
    author: String,
    assets: ThemeAssets,
    quoteFontSize: Int,
    authorFontSize: Int,
    iconPadding: Dp,
) {
    Text(
        text = quote,
        style = TextStyle(
            color = androidx.glance.color.ColorProvider(assets.textColor, assets.textColor),
            fontSize = quoteFontSize.sp,
        ),
        modifier = GlanceModifier.fillMaxWidth(),
    )
    Box(
        modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(iconPadding),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(assets.iconBitmap),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = GlanceModifier.fillMaxSize(),
        )
    }
    if (author.isNotEmpty()) {
        Text(
            text = author,
            style = TextStyle(
                color = androidx.glance.color.ColorProvider(assets.hintColor, assets.hintColor),
                fontSize = authorFontSize.sp,
                textAlign = TextAlign.End,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
    }
}
