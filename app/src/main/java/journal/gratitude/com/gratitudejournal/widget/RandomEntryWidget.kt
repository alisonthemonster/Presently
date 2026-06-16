package journal.gratitude.com.gratitudejournal.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.ContextThemeWrapper
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.AndroidEntryPoint
import journal.gratitude.com.gratitudejournal.ContainerActivity
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.repository.EntryRepository
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings
import journal.gratitude.com.gratitudejournal.ui.theme.PresentlyThemeSpec
import journal.gratitude.com.gratitudejournal.ui.theme.resolvePresentlyThemeTokens
import journal.gratitude.com.gratitudejournal.util.toFullString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RandomEntryWidget : AppWidgetProvider() {

    @Inject
    lateinit var repository: EntryRepository

    @Inject
    lateinit var settings: PresentlySettings

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, RandomEntryWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)

            // Notify the ListView that the dataset changed so it pulls a fresh entry
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list)

            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        scope.launch {
            val entry = repository.getRandomEntry()
            val views = RemoteViews(context.packageName, R.layout.random_entry_widget)

            val themeName = settings.getCurrentTheme()
            val themeSpec = PresentlyThemeSpec.fromStorageValue(themeName)
            val themedContext = ContextThemeWrapper(context, themeSpec.styleRes)
            val tokens = themedContext.resolvePresentlyThemeTokens()

            // Resolve dynamic colors
            val backgroundColor = tokens.timelineBackground.toArgb()
            val bodyColor = tokens.timelineBody.toArgb()
            val hintColor = tokens.timelineHint.toArgb()

            // Apply backgrounds and button colors in the main layout
            views.setInt(R.id.widget_background, "setColorFilter", backgroundColor)
            views.setInt(R.id.widget_refresh_button, "setColorFilter", bodyColor)

            // Package entry data and colors to deliver to the WidgetScrollService
            val serviceIntent = Intent(context, WidgetScrollService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)

                if (entry != null) {
                    putExtra(EXTRA_ENTRY_CONTENT, entry.entryContent)
                    putExtra(EXTRA_ENTRY_DATE, entry.entryDate.toFullString())
                } else {
                    putExtra(EXTRA_ENTRY_CONTENT, context.getString(R.string.widget_no_entries))
                    putExtra(EXTRA_ENTRY_DATE, "")
                }

                putExtra(EXTRA_COLOR_BODY, bodyColor)
                putExtra(EXTRA_COLOR_HINT, hintColor)

                // Embed unique URI data to force system to update the adapter instance
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }

            // Bind adapter service to your layout's ListView
            views.setRemoteAdapter(R.id.widget_list, serviceIntent)

            // NEW: Set up a PendingIntent template for the ListView items.
            // When an item inside the list is clicked, it appends its own extras to this base Intent.
            val clickIntent = Intent(context, ContainerActivity::class.java).apply {
                action = ACTION_OPEN_ENTRY
            }
            val clickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                // Must be FLAG_MUTABLE so the system can merge the fill-in intent data into it
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent)

            // Click to refresh
            val refreshIntent = Intent(context, RandomEntryWidget::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPendingIntent = PendingIntent.getBroadcast(
                context, 0, refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_refresh_button, refreshPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    companion object {
        private const val ACTION_REFRESH = "journal.gratitude.com.gratitudejournal.widget.ACTION_REFRESH"

        // New explicit action and key names for opening entries
        const val ACTION_OPEN_ENTRY = "journal.gratitude.com.gratitudejournal.widget.ACTION_OPEN_ENTRY"
        const val EXTRA_SELECTED_DATE = "extra_selected_date"

        // Intent bundle string constants for your Service to parse
        const val EXTRA_ENTRY_CONTENT = "extra_entry_content"
        const val EXTRA_ENTRY_DATE = "extra_entry_date"
        const val EXTRA_COLOR_BODY = "extra_color_body"
        const val EXTRA_COLOR_HINT = "extra_color_hint"
    }
}
