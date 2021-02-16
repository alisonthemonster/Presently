package com.presently.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.os.Bundle
import android.widget.RemoteViews

/**
 * A daily gratitude quote widget. It pulls gratitude quotes from our inspirations.xml resource file
 * once a day. It also supports several widget sizes.
 */
class GratitudeQuoteWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        updateAppWidget(context, appWidgetManager, appWidgetId)
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }
}

/**
 * Updates a widget with new copy and the correct layout.
 */
internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val quotes = context.resources.getStringArray(R.array.inspirations)
    val shortStrings = mutableListOf<String>()
    val mediumStrings =  mutableListOf<String>()
    val longStrings =  mutableListOf<String>()
    val veryLongStrings =  mutableListOf<String>()
    for (quote in quotes) {
        when (quote.length) {
            in 1..70 -> {
                shortStrings.add(quote)
                mediumStrings.add(quote)
                longStrings.add(quote)
                veryLongStrings.add(quote)
            }
            in 70..90 -> {
                mediumStrings.add(quote)
                longStrings.add(quote)
                veryLongStrings.add(quote)
            }
            in 90..150 -> {
                longStrings.add(quote)
                veryLongStrings.add(quote)
            }
            else -> veryLongStrings.add(quote)
        }
    }

    val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
    val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
    val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
    val size = getViewSize(minWidth, minHeight)

    val quoteString = when (size) {
        WidgetSize.SMALL -> shortStrings.random()
        WidgetSize.MEDIUM -> mediumStrings.random()
        WidgetSize.LARGE -> longStrings.random()
        WidgetSize.XLARGE -> veryLongStrings.random()
    }

    val splitString = quoteString.split("\n")
    val quote = splitString.first()
    val author = splitString.last()

    val remoteViews = getRemoteViewForSize(context, size)

    remoteViews.setTextViewText(R.id.quote_text, quote)
    remoteViews.setTextViewText(R.id.author_text, author)

    appWidgetManager.updateAppWidget(appWidgetId, remoteViews)
}

/**
 * Returns the RemoteViews for the current widget size
 *
 * @param context Context
 * @param size The WidgetSize of the widget
 * @return RemoteView
 */
internal fun getRemoteViewForSize(context: Context, size: WidgetSize): RemoteViews {
    if (size == WidgetSize.SMALL) return RemoteViews(context.packageName, R.layout.small_quote)
    if (size == WidgetSize.MEDIUM) return RemoteViews(context.packageName, R.layout.medium_quote)
    if (size == WidgetSize.LARGE) return RemoteViews(context.packageName, R.layout.large_quote)
    else return RemoteViews(context.packageName, R.layout.extra_large_quote)
}

internal fun getViewSize(minWidth: Int, minHeight: Int): WidgetSize  {
    val rows: Int = getCellsForSize(minHeight)
    val columns: Int = getCellsForSize(minWidth)
    val area = rows * columns
    if (rows == 1 && columns <= 3 || area <= 3) return WidgetSize.SMALL
    if (rows == 1 && columns > 3 || area <= 4) return WidgetSize.MEDIUM
    if (area <= 8) return WidgetSize.LARGE
    else return WidgetSize.XLARGE
}

enum class WidgetSize {
    SMALL,
    MEDIUM,
    LARGE,
    XLARGE
}

/**
 * Returns number of cells needed for given size of the widget.
 *
 * @param size Widget size in dp.
 * @return Size in number of cells.
 */
private fun getCellsForSize(size: Int): Int {
    var n = 2
    while (70 * n - 30 < size) {
        ++n
    }
    return n - 1
}
