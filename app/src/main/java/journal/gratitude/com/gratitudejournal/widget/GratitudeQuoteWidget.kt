package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
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

class GratitudeQuoteWidget : GlanceAppWidget() {

    companion object {
        val MEDIUM = DpSize(110.dp, 110.dp)
        val LARGE = DpSize(180.dp, 110.dp)
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(MEDIUM, LARGE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val themeName = prefs.getString(THEME_PREF, "original") ?: "original"
        val theme = PresentlyThemeSpec.fromStorageValue(themeName)

        val themedContext = ContextThemeWrapper(context, theme.styleRes)
        val attrs = themedContext.obtainStyledAttributes(
            intArrayOf(
                R.attr.timelineBackgroundColor,
                R.attr.timelineLineColor,
                R.attr.timelineHintColor,
            )
        )
        val backgroundColor = Color(attrs.getColor(0, 0xFFDBD1C7.toInt()))
        val textColor = Color(attrs.getColor(1, 0xFF000000.toInt()))
        val hintColor = Color(attrs.getColor(2, 0xFF79736A.toInt()))
        attrs.recycle()

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
            )
        }
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
        when (LocalSize.current) {
            GratitudeQuoteWidget.LARGE -> QuoteLayout(quote, author, textColor, hintColor, quoteFontSize = 18, authorFontSize = 13)
            else                       -> QuoteLayout(quote, author, textColor, hintColor, quoteFontSize = 14, authorFontSize = 12)
        }
    }
}

@Composable
private fun QuoteLayout(
    quote: String,
    author: String,
    textColor: Color,
    hintColor: Color,
    quoteFontSize: Int,
    authorFontSize: Int,
) {
    Box(modifier = GlanceModifier.fillMaxSize()) {
        Text(
            text = quote,
            style = TextStyle(
                color = ColorProvider(textColor),
                fontSize = quoteFontSize.sp,
            ),
            modifier = GlanceModifier.fillMaxWidth(),
        )
        if (author.isNotEmpty() && authorFontSize > 0) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Vertical.Bottom,
            ) {
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
}
