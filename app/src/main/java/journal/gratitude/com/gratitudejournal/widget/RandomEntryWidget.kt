package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dagger.hilt.android.EntryPointAccessors
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.di.EntryRepositoryEntryPoint
import journal.gratitude.com.gratitudejournal.di.SettingsEntryPoint
import journal.gratitude.com.gratitudejournal.model.Entry
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDate

class RandomEntryWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        const val EXTRA_SELECTED_DATE = "extra_selected_date"
        val selectedDateKey = ActionParameters.Key<String>("selected_date")

        private val SMALL_SQUARE = DpSize(100.dp, 100.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(250.dp, 100.dp)
        private val TALL = DpSize(100.dp, 250.dp)
        private val BIG_SQUARE = DpSize(250.dp, 250.dp)

        private const val PREVIEW_DATE = "2026-03-28"
        private const val PREVIEW_CONTENT = "I am grateful for a beautiful sunny day."
        private const val PREVIEW_THEME = "original"

        val KEY_ENTRY_DATE = stringPreferencesKey("entry_date")
        val KEY_ENTRY_CONTENT = stringPreferencesKey("entry_content")
    }

    override val sizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE),
    )

    override val previewSizeMode = SizeMode.Responsive(
        setOf(SMALL_SQUARE, HORIZONTAL_RECTANGLE, TALL, BIG_SQUARE)
    )

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val assets = loadThemeAssets(context, PREVIEW_THEME)
        val entry = Entry(LocalDate.parse(PREVIEW_DATE), PREVIEW_CONTENT)
        provideContent {
            WidgetContent(
                entry = entry,
                isLocked = false,
                assets = assets,
            )
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context, EntryRepositoryEntryPoint::class.java)
        val entryRepository = entryPoint.entryRepository()
        val dispatchers = entryPoint.coroutineDispatchers()
        val settings = EntryPointAccessors.fromApplication(context, SettingsEntryPoint::class.java).settings

        val newEntry = withContext(dispatchers.io) {
            entryRepository.getRandomEntry()
        }

        updateAppWidgetState(context, PreferencesGlanceStateDefinition, id) { prefs ->
            prefs.toMutablePreferences().apply {
                if (newEntry != null) {
                    this[KEY_ENTRY_DATE] = newEntry.entryDate.toString()
                    this[KEY_ENTRY_CONTENT] = newEntry.entryContent
                } else {
                    remove(KEY_ENTRY_DATE)
                    remove(KEY_ENTRY_CONTENT)
                }
            }
        }

        val isLocked = settings.isBiometricsEnabled() && settings.shouldLockApp()
        val assets = loadThemeAssets(context, settings.getCurrentTheme())

        provideContent {
            val prefs = currentState<Preferences>()
            val entryDate = prefs[KEY_ENTRY_DATE]
            val entryContent = prefs[KEY_ENTRY_CONTENT]

            val entry = if (entryDate != null && entryContent != null) {
                Entry(LocalDate.parse(entryDate), entryContent)
            } else {
                null
            }

            WidgetContent(
                entry = entry,
                isLocked = isLocked,
                assets = assets,
            )
        }
    }
}

@Composable
private fun WidgetContent(
    entry: Entry?,
    isLocked: Boolean,
    assets: ThemeAssets,
) {
    val actionParameters = if (entry != null && !isLocked) {
        actionParametersOf(
            RandomEntryWidget.selectedDateKey to entry.entryDate.toString(),
            GratitudeQuoteWidget.cameFromWidgetKey to true
        )
    } else {
        actionParametersOf(GratitudeQuoteWidget.cameFromWidgetKey to true)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(assets.backgroundColor)
            .cornerRadius(16.dp)
            .clickable(actionStartActivity<ContainerActivity>(parameters = actionParameters))
    ) {
        val size = LocalSize.current
        val (headerFontSize, contentFontSize) = when {
            size.width >= 250.dp && size.height >= 250.dp -> 16 to 16
            size.height >= 250.dp -> 14 to 14
            else -> 12 to 12
        }

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
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
private fun ColumnScope.LockedContent(assets: ThemeAssets, fontSize: Int) {
    Box(
        modifier = GlanceModifier.fillMaxSize().defaultWeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = LocalContext.current.getString(R.string.unlock_to_view_entries),
            style = TextStyle(
                color = ColorProvider(assets.textColor, assets.textColor),
                fontSize = fontSize.sp,
                textAlign = androidx.glance.text.TextAlign.Center
            )
        )
    }
}

@Composable
private fun ColumnScope.NoEntriesContent(assets: ThemeAssets, fontSize: Int) {
    Box(
        modifier = GlanceModifier.fillMaxSize().defaultWeight(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = LocalContext.current.getString(R.string.widget_no_entries),
            style = TextStyle(
                color = ColorProvider(assets.textColor, assets.textColor),
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
            color = ColorProvider(assets.hintColor, assets.hintColor),
            fontSize = headerFontSize.sp,
            fontWeight = FontWeight.Bold
        )
    )
    Box(modifier = GlanceModifier.padding(vertical = 4.dp)) {}
    Text(
        text = entry.entryContent,
        style = TextStyle(
            color = ColorProvider(assets.textColor, assets.textColor),
            fontSize = contentFontSize.sp
        ),
        modifier = GlanceModifier.fillMaxWidth().defaultWeight()
    )
    Box(
        modifier = GlanceModifier.fillMaxWidth().padding(top = 4.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Image(
            provider = ImageProvider(assets.iconBitmap),
            contentDescription = null,
            modifier = GlanceModifier.size(24.dp)
        )
    }
}
