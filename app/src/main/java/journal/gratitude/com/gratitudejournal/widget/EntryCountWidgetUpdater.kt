package journal.gratitude.com.gratitudejournal.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import journal.gratitude.com.gratitudejournal.room.EntryDao
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the Entry count widget current by observing the entries table, so every write path
 * (saving, deleting, restoring a backup) is covered without the data layer knowing about widgets.
 *
 * Redraws only when the count actually changes, and runs on its own process-lifetime scope so
 * a slow widget update can never delay saving an entry.
 */
@Singleton
class EntryCountWidgetUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
    private val entryDao: EntryDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun start() {
        scope.launch {
            try {
                entryDao.getEntryCount()
                    .distinctUntilChanged()
                    .drop(1) // the widget renders its own initial value
                    .collect { EntryCountWidget().updateAll(context) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // The widget is a nicety: never let it affect the app itself.
                Log.w("EntryCountWidgetUpdater", "Stopped refreshing the widget", e)
            }
        }
    }
}
