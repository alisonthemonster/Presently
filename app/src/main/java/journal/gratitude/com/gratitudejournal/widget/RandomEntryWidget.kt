package journal.gratitude.com.gratitudejournal.widget

import android.annotation.SuppressLint
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
import androidx.core.graphics.createBitmap
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
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.EntryRepositoryEntryPoint
import journal.gratitude.com.gratitudejournal.di.SettingsEntryPoint
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.util.toFullString
import org.threeten.bp.format.DateTimeFormatter

class RandomEntryWidget : GlanceAppWidget() {

    companion object {
        const val ACTION_REFRESH = "journal.gratitude.com.gratitudejournal.widget.ACTION_REFRESH"
        const val EXTRA_SELECTED_DATE = "extra_selected_date"
        val selectedDateKey = ActionParameters.Key<String>("selected_date")

        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val TALL = DpSize(100.dp, 250.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)

        private const val ICON_SIZE_PX = 192
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryRepository = EntryPointAccessors
            .fromApplication(context, EntryRepositoryEntryPoint::class.java)
            .entryRepository()
        val settings = EntryPointAccessors
            .fromApplication(context, SettingsEntryPoint::class.java)
            .settings

        val entry = entryRepository.getRandomEntry()
        val isLocked = settings.isBiometricsEnabled() && settings.shouldLockApp()
        val assets = loadThemeAssets(context, settings.getCurrentTheme())

        provideContent {
            WidgetContent(
                entry = entry,
                isLocked = isLocked,
                assets = assets,
            )
        }
    }

    @SuppressLint("ResourceType")
    private fun loadThemeAssets(context: Context, themeName: String): ThemeAssets {
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

        val iconBitmap = rasterizeDrawable(themedContext, iconResId, ICON_SIZE_PX)
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
}

@Composable
private fun WidgetContent(
    entry: Entry?,
    isLocked: Boolean,
    assets: ThemeAssets,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(assets.backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<ContainerActivity>(
                parameters = if (entry != null && !isLocked) {
                    actionParametersOf(RandomEntryWidget.selectedDateKey to entry.entryDate.toString())
                } else {
                    actionParametersOf()
                }
            ))
            .padding(12.dp),
        contentAlignment = Alignment.TopStart,
    ) {
        val size = LocalSize.current
        val (headerFontSize, contentFontSize) = when {
            size.width >= 250.dp && size.height >= 250.dp -> 16 to 16
            size.height >= 250.dp -> 14 to 14
            else -> 12 to 12
        }

        Column(modifier = GlanceModifier.fillMaxSize()) {
            if (isLocked) {
                LockedContent(assets, contentFontSize)
            } else if (entry == null) {
                NoEntriesContent(assets, contentFontSize)
            } else {
                EntryContent(entry, assets, headerFontSize, contentFontSize)
            }
        }
    }
}

@Composable
private fun LockedContent(assets: ThemeAssets, fontSize: Int) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = androidx.glance.LocalContext.current.getString(R.string.unlock_to_view_entries),
            style = TextStyle(
                color = androidx.glance.color.ColorProvider(assets.textColor, assets.textColor),
                fontSize = fontSize.sp,
                textAlign = androidx.glance.text.TextAlign.Center
            )
        )
    }
}

@Composable
private fun NoEntriesContent(assets: ThemeAssets, fontSize: Int) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = androidx.glance.LocalContext.current.getString(R.string.widget_no_entries),
            style = TextStyle(
                color = androidx.glance.color.ColorProvider(assets.textColor, assets.textColor),
                fontSize = fontSize.sp,
                textAlign = androidx.glance.text.TextAlign.Center
            )
        )
    }
}

@Composable
private fun ColumnScope.EntryContent(
    entry: Entry,
    assets: ThemeAssets,
    headerFontSize: Int,
    contentFontSize: Int
) {
    Text(
        text = entry.entryDate.toFullString(),
        style = TextStyle(
            color = androidx.glance.color.ColorProvider(assets.hintColor, assets.hintColor),
            fontSize = headerFontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Box(modifier = GlanceModifier.padding(vertical = 4.dp)) {}
    Text(
        text = entry.entryContent,
        style = TextStyle(
            color = androidx.glance.color.ColorProvider(assets.textColor, assets.textColor),
            fontSize = contentFontSize.sp
        ),
        modifier = GlanceModifier.fillMaxWidth().defaultWeight()
    )
    Box(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        contentAlignment = Alignment.BottomEnd
    ) {
        Image(
            provider = ImageProvider(assets.iconBitmap),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
    }
}
