package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import dagger.hilt.android.EntryPointAccessors
import android.util.Log
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import journal.gratitude.com.gratitudejournal.R
import journal.gratitude.com.gratitudejournal.settings.PresentlySettings

class WidgetScrollService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetScrollFactory(this.applicationContext, intent)
    }
}

class WidgetScrollFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var contentText: String = ""
    private var dateText: String = ""
    private var bodyColor: Int = 0xFFFFFF
    private var hintColor: Int = 0xCCCCCC

    // Define a Hilt EntryPoint interface to fetch PresentlySettings without a constructor
    @EntryPoint
    @dagger.hilt.InstallIn(SingletonComponent::class)
    interface WidgetSettingsEntryPoint {
        fun presentlySettings(): PresentlySettings
    }

    private lateinit var settings: PresentlySettings

    override fun onCreate() {
        // Resolve PresentlySettings using Hilt's EntryPointAccessors helper
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetSettingsEntryPoint::class.java
        )
        settings = entryPoint.presentlySettings()

        extractIntentData()
    }

    override fun onDataSetChanged() {
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            WidgetSettingsEntryPoint::class.java
        )
        settings = entryPoint.presentlySettings()

        extractIntentData()
    }

    private fun extractIntentData() {
        contentText = intent.getStringExtra(RandomEntryWidget.EXTRA_ENTRY_CONTENT) ?: ""
        dateText = intent.getStringExtra(RandomEntryWidget.EXTRA_ENTRY_DATE) ?: ""
        bodyColor = intent.getIntExtra(RandomEntryWidget.EXTRA_COLOR_BODY, 0xFFFFFF)
        hintColor = intent.getIntExtra(RandomEntryWidget.EXTRA_COLOR_HINT, 0xCCCCCC)
    }

    override fun onDestroy() {}

    override fun getCount(): Int = 1

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_row)

        // Securely check if biometrics are active before revealing journal entries
        val shouldLock = settings.isBiometricsEnabled() && settings.shouldLockApp()
        Log.d("WidgetScrollFactory", "getViewAt: shouldLock: $shouldLock")
        if (shouldLock) {
            views.setTextViewText(R.id.widget_content, context.getString(R.string.unlock_to_view_entries, "Unlock the app to view your journal entries."))
            views.setTextViewText(R.id.widget_date, "")
        } else {
            views.setTextViewText(R.id.widget_content, contentText)
            views.setTextViewText(R.id.widget_date, dateText)
        }

        views.setTextColor(R.id.widget_content, bodyColor)
        views.setTextColor(R.id.widget_date, hintColor)

        val fillInIntent = Intent().apply {
            putExtra(RandomEntryWidget.EXTRA_SELECTED_DATE, dateText)
        }
        views.setOnClickFillInIntent(R.id.widget_row_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}
